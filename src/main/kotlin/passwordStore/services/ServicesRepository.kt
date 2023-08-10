/*
 *
 * MIT License
 *
 * Copyright (c) 2020 Mirko Sciachero
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package passwordStore.services

import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import mu.KotlinLogging
import passwordStore.audit.Action
import passwordStore.audit.AuditMessage
import passwordStore.audit.Event
import passwordStore.audit.EventBus
import passwordStore.crypto.CryptExtension
import passwordStore.sql.performTransaction
import passwordStore.sql.query
import passwordStore.sql.saveOrUpdate
import passwordStore.sql.singleRow
import passwordStore.users.User
import passwordStore.utils.*
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource
import kotlin.math.max


class ServicesRepository(
    private val datasource: DataSource,
    private val eventBus: EventBus,
    private val cryptExtension: CryptExtension
) {

    fun String.decrypt() = cryptExtension.decrypt(this)

    fun String.crypt() = cryptExtension.crypt(this)

    private fun asService(rs: ResultSet, mode: Mode = Mode.FETCH): Service {
        val tags = when (mode) {
            Mode.FETCH -> fetchTags(rs.getLong("id"))
            Mode.SPLIT -> rs.getString("tags")?.split(",") ?: listOf()
        }
        return Service(
            rs.getString("service"),
            rs.getString("username"),
            rs.getString("password").decrypt(),
            rs.getString("note").decrypt(),
            false,
            rs.getTimestamp("lastUpdate").toLocalDateTime().toKotlinLocalDateTime(),
            rs.getString("userid"), 1.0, tags
        )
    }

    private fun fetchTags(serviceId: Long): List<String> =
        datasource.query(
            """select tag 
                    from tags t, service_tags st 
                    where t.id = st.id_tag 
                    and st.id_service = :serviceId""".trimIndent(), mapOf("serviceId" to serviceId)
        ) { it.getString(1) }


    suspend fun search(user: User, pattern: String = "", tag: String = ""): List<Service> {
        fun score(s: Service): Double =
            if (pattern.isBlank()) {
                1.0
            } else {
                max(
                    pattern.distance(s.service),
                    pattern.distance(s.username)
                )
            }

        LOGGER.info("User $user search for $pattern and tag $tag")
        val result = if (tag.isBlank()) {
            datasource.query(
                """ select * from services
            where userid = :user
            """, mapOf("user" to user.userid)
            ) { rs -> asService(rs, Mode.FETCH) }
        } else {
            LOGGER.debug("""Query for a tag "$tag" """)
            datasource.query(
                """ select s.* from services s, service_tags st, tags t
            where userid = :user
                and s.id = st.id_service
                and st.id_tag = t.id
                and t.tag = :tag
            """, mapOf("user" to user.userid, "tag" to tag)
            ) { rs -> asService(rs, Mode.FETCH) }
        }
        LOGGER.debug { "Result is $result" }
        return result
            .map { s ->
                val rate = score(s)
                LOGGER.debug("Service $s match a score of $rate")
                s.copy(score = rate)
            }
            .filter { it.score >= 0.5 }
            .sortedWith { s1, s2 ->
                s2.score.compareTo(s1.score).let { result ->
                    if (result == 0) {
                        s1.service.compareTo(s2.service)
                    } else {
                        result
                    }
                }
            }

    }

    private suspend fun send(event: Event) {
        val message = AuditMessage(event)
        eventBus.send(message)
    }

    @Throws(SQLException::class)
    suspend fun store(service: Service): Service {
        LOGGER.debug("Storing service {}", service)
        check(service.validate().isSuccess) {
            "Service ${service.service} is not valid"
        }
        datasource.performTransaction {
            val insertedRows = this.saveOrUpdate(
                """ insert into services (service, username, password, note, lastUpdate, userid)
              values (?, ?, ?, ?, ?, ?)
            """,
                service.service,
                service.username,
                service.password.crypt(),
                service.note.crypt(),
                service.updateTime.toTimestamp(timezone),
                service.userid
            )
            assert(insertedRows > 0)
            val id = findServiceId(service.service, service.userid)
            service.tags.forEach {
                val tag = it.replaceFirstChar { c -> c.uppercase() }
                addServiceToTag(service = id, tag = tag)
            }
        }
        send(Event(service, Action.insert))
        LOGGER.debug("Stored service {}", service)
        return findByName(service.service, service.userid)
    }

    private fun Connection.addServiceToTag(tag: String, service: Long) {
        saveOrUpdate(
            """insert into TAGS(tag) select * from (select cast(? as VARCHAR(50)) new_tag) t where not exists (select 1 from tags ts where ts.tag = ?)  """,
            tag,
            tag
        )
        saveOrUpdate(
            """insert into SERVICE_TAGS(id_service, id_tag) select ?, id from tags t where t.tag = ?  """,
            service,
            tag
        )
    }

    @Throws(IllegalStateException::class)
    suspend fun findByName(serviceName: String, userId: String): Service = datasource.singleRow(
        """ select * from services
            where service = :serviceName and userId= :userId
          """, mapOf("serviceName" to serviceName, "userId" to userId)
    ) { rs -> asService(rs, Mode.FETCH) }

    private fun Connection.findServiceId(serviceName: String, userId: String): Long =
        this.singleRow(
            """
                select id from services 
                where service = :serviceName 
                and userid = :userId
                """.trimIndent(), mapOf("serviceName" to serviceName, "userId" to userId)
        ) {
            it.getLong("id")
        }


    @Throws(SQLException::class)
    suspend fun delete(serviceName: String, userId: String) {
        val service = findByName(serviceName, userId)
        datasource.performTransaction {
            val id = findServiceId(serviceName, userId)
            saveOrUpdate("""delete from service_tags where id_service = ?""", id)
            saveOrUpdate("""delete from services where service = ? and userid = ?""", serviceName, userId)
        }
        send(Event(service, Action.delete))
    }

    @Throws(SQLException::class)
    suspend fun update(service: Service): Service {
        val userId = service.userid
        val oldService = findByName(service.service, userId)
        val tagsToAdd = service.tags.map(String::titlecase).toMutableSet().apply {
            removeAll(oldService.tags.map(String::titlecase).toSet())
        }

        val tagsToDelete = oldService.tags.map(String::titlecase).toMutableSet().apply {
            removeAll(service.tags.map(String::titlecase).toSet())
        }
        datasource.performTransaction {
            this.saveOrUpdate(
                """update services
              set username = ?,
              password = ?,
              note = ?,
              lastUpdate = ?,
              userid = ?
              where service = ?""", service.username, service.password.crypt(),
                service.note.crypt(), service.updateTime.toTimestamp(timezone), userId, service.service
            ).also { rows ->
                check(rows > 0) {
                    "No rows updated"
                }
                val id = findServiceId(service.service, service.userid)
                tagsToDelete.forEach {
                    this.saveOrUpdate(
                        """delete from SERVICE_TAGS st 
                                    where st.id_service = ? 
                                    and st.id_tag = (
                                    select id from tags where tag = ?
                                    )""".trimIndent(), id, it
                    )
                }
                tagsToAdd.forEach {
                    addServiceToTag(service = id, tag = it)
                }
            }
        }

        send(Event(oldService, Action.update))
        return findByName(service.service, service.userid)
    }


    suspend fun history(service: String, exactMatch: Boolean, user: User): List<Event> {
        return if (exactMatch) {
            exactMatchResult(user, service)
        } else {
            fuzzyMatchResult(user, service)
        }
    }

    private fun fuzzyMatchResult(user: User, servPattern: String): List<Event> {
        return datasource.query(
            """ select * from services_hist h
                where ( exists (select 1 from services s where s.service = h.service and s.userid = :userid)
                  or h.userid = :userid)
              """, mapOf("userid" to user.userid)
        ) { rs ->
            Event(
                asService(rs, Mode.SPLIT),
                Action.valueOf(rs.getString("operation")),
                rs.getTimestamp("operation_date").toKotlinDateTime()
            )
        }.map { ev ->
            val rate = if (servPattern.isNotBlank()) {
                servPattern.distance(ev.service.service)
            } else {
                1.0
            }
            LOGGER.debug("Service $ev has score $rate")
            ev.copy(service = ev.service.copy(score = rate))
        }.filter { ev ->
            LOGGER.debug("Filtering event $ev")
            ev.service.score >= 0.5
        }.sortedWith { ev1, ev2 ->
            val result = ev2.service.score.compareTo(ev1.service.score)
            if (result == 0) {
                -1 * ev1.actionDate.compareTo(ev2.actionDate)
            } else {
                result
            }
        }
    }

    private fun exactMatchResult(user: User, servPattern: String): List<Event> {
        LOGGER.info("Querying exact service $servPattern for $user")
        return datasource.query(
            """ select * from services_hist h
                where service = :service
                and h.userid = :userid
                order by service, operation_date
              """, mapOf("service" to servPattern, "userid" to user.userid)
        ) { rs ->
            Event(
                asService(rs, Mode.SPLIT),
                Action.valueOf(rs.getString("operation")),
                rs.getTimestamp("operation_date").toKotlinDateTime()
            )
        }
    }


    companion object {
        enum class Mode { FETCH, SPLIT }

        private val LOGGER = KotlinLogging.logger { }
    }
}


data class Service(
    var service: String = "",
    var username: String = "",
    var password: String = "",
    var note: String = "",
    var dirty: Boolean = false,
    var updateTime: LocalDateTime = Clock.System.currentDateTime(),
    var userid: String = "",
    var score: Double = 0.0,
    var tags: List<String> = listOf()
) {
    fun trim(): Service =
        this.copy(service = this.service.trim(), username = this.username.trim(), password = this.password.trim())

    fun validate(): Result<Unit> =
        runCatching {
            check(service.isNotEmpty()) {
                "Service name must be present"
            }
            check(username.isNotEmpty()) {
                "Username must be present"
            }
            check(password.isNotEmpty()) {
                "Password must be present"
            }
        }

}



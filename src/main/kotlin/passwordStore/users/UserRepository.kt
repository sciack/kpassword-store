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

package passwordStore.users

import mu.KotlinLogging
import passwordStore.crypto.CryptExtension.Companion.hash
import passwordStore.crypto.CryptExtension.Companion.verify
import passwordStore.services.ServicesRepository
import passwordStore.sql.query
import passwordStore.sql.saveOrUpdate
import passwordStore.sql.singleRow
import java.security.Principal
import java.sql.SQLException
import javax.sql.DataSource

class UserRepository(
    private val ds: DataSource,
    private val servicesRepository: ServicesRepository,
) {

    @Throws(SQLException::class)
    fun insertUser(user: AddUser) {
        ds.saveOrUpdate("""insert into PS_User (userid, email, password, fullname, role) 
            values (?, ?, ?,?, ?)
        """.trimIndent(),
            user.userid,
            user.email,
            user.password.hash(),
            user.fullName,
            user.roles.joinToString { it.name })
    }


    private fun String.asSetOfRoles(): Set<Roles> = this.trim().split(",")
        .filterNot(String::isEmpty).map {
            LOGGER.warn { "Looking for role: $it" }
            Roles.valueOf(it.trim())
        }.toSet()

    fun list() = ds.query(
        """select userid, email, fullname, role, 
                    (select count(*) from services where userid = u.userid) as services 
                    from PS_User u
        """.trimIndent()
    ) { rs ->
        ListUser(
            rs.getString("userid"),
            rs.getString("email"),
            rs.getString("fullname"),
            rs.getString("role").asSetOfRoles(),
            rs.getInt("services")
        )
    }


    fun login(userid: String, password: String): User {

        return ds.singleRow(
            """ select id, userid, email, userid, password, fullname, role
          from ps_user
          where userid = ?
        """, userid
        ) { rs ->
            require(rs.getString("password").verify(password)) {
                "Password for user $userid is wrong"
            }
            User(
                id = rs.getInt("id"),
                fullName = rs.getString("fullname"),
                email = rs.getString("email"),
                userid = rs.getString("userid"),
                roles = rs.getString("role").asSetOfRoles()
            ).also { LOGGER.debug("User: $it") }
        }
    }

    fun findUser(userid: String): User {

        return ds.singleRow(
            """ select id, userid, email, userid, password, fullname, role
          from ps_user
          where userid = ?
        """, userid
        ) { rs ->

            User(
                id = rs.getInt("id"),
                fullName = rs.getString("fullname"),
                email = rs.getString("email"),
                userid = rs.getString("userid"),
                roles = rs.getString("role").asSetOfRoles()
            ).also { LOGGER.debug("User: $it") }
        }
    }

    fun updateUser(user: UpdateUser, principal: Principal): User {
        val params = mutableListOf(user.fullName)
        val statement = buildString {
            append(
                """update ps_user 
            set fullname = ?,
            """
            )
            if (user.password.isNotEmpty()) {
                append(" password = ?,")
                append("\n")
                params.add(user.password.hash())
            }
            append(
                """    
                email = ?,
                role = ?
            where userid = ?
            
        """
            )
            params.add(user.email)
            params.add(user.roles.joinToString { it.name })
            params.add(user.userid)
        }.trimIndent()
        ds.saveOrUpdate(
            statement, *params.toTypedArray()
        )
        return findUser(user.userid)
    }


    suspend fun deleteUser(userid: String) {
        check(servicesRepository.search(findUser(userid)).isEmpty()) {
            "The user has service stored, before delete the user all the service must be deleted"
        }
        ds.saveOrUpdate(
            """
            delete from ps_user 
            where userid = ?
        """.trimIndent(), userid
        ).also {
            if (it == 0) throw IllegalArgumentException("User $userid not exists")
        }

    }

    companion object {
        private val LOGGER = KotlinLogging.logger {}

    }
}
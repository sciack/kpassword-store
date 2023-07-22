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
import passwordStore.sql.query
import passwordStore.sql.saveOrUpdate
import passwordStore.sql.singleRow
import java.security.Principal
import java.sql.SQLException
import javax.sql.DataSource

class UserRepository(
    private val ds: DataSource,
    //private val servicesRepository: ServicesRepository,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


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


    private fun String.asSetOfRoles(): Set<Roles> = this.split(",").map {
        Roles.valueOf(it.trim())
    }.toSet()

    fun get(param: String?) =
        ds.query(
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
                token = "",
                roles = rs.getString("role").asSetOfRoles()
            ).also { logger.debug("User: $it") }
        }
    }

    fun updateUser(user: UpdateUser, principal: Principal): User {
        ds.saveOrUpdate(
            """update ps_user 
            set fullname = ?,
                password = ?,
                email = ?
            where userid = ?
            
        """.trimIndent(), user.fullName, user.password.hash(), user.email, principal.name
        )
        return login(user.userid, user.password)
    }

    fun updateUser(user: AddUser, principal: Principal): User {
        ds.saveOrUpdate(
            """update ps_user 
            set fullname = ?,
                password = ?,
                email = ?,
                role = ?
            where userid = ?
            
        """.trimIndent(),
            user.fullName,
            user.password.hash(),
            user.email,
            user.roles.joinToString { it.name },
            user.userid
        ).also {
            if (it == 0) throw IllegalArgumentException("User ${user.userid} does not exists")
        }
        return login(user.userid, user.password)
    }

    /*
    fun deleteUser(userid: String, principal: Principal) {
        runBlocking {
            check(servicesRepository.search(userid).isEmpty()) {
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
    }
     */
}
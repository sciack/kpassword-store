package passwordStore.tags

import passwordStore.sql.query
import passwordStore.users.User
import java.sql.SQLException
import javax.sql.DataSource

class TagRepository(private val dataSource: DataSource) {


    @Throws(SQLException::class)
    suspend fun tags(user: User): TagElement {
        return dataSource.query(
            """select tag, 
            (   
                select count(1) from service_tags st, services s 
                where t.id = st.id_tag
                  and st.id_service = s.id
                  and s.userid = :userid
            ) 
            from tags t""".trimIndent(), mapOf("userid" to user.userid)
        ) {
            it.getString(1) to it.getInt(2)
        }.toMap()
    }
}

typealias TagElement = Map<String, Int>
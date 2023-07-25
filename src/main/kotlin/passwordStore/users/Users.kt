package passwordStore.users

class Users {
}


data class AddUser(
    var userid: String = "",
    var email: String = "",
    var fullName: String = "",
    var password: String = "",
    var roles: Set<Roles> = setOf(Roles.NormalUser)
)

data class ListUser(
    val userid: String,
    val email: String,
    val fullName: String,
    val roles: Set<Roles> = setOf(),
    val services: Int = 0
)

enum class Roles { NormalUser, Administrator }

data class User(val id: Int,
                val fullName: String,
                val userid: String,
                val email: String,
                val roles: Set<Roles> = setOf())


data class UpdateUser(var fullName: String = "", var email: String = "", var password: String = "", var userid:String = "")

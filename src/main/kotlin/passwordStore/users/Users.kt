package passwordStore.users


data class ListUser(
    val userid: String,
    val email: String,
    val fullName: String,
    val roles: Set<Roles> = setOf(),
    val services: Int = 0
)

enum class Roles { NormalUser, Administrator }

data class User(
    val id: Int,
    val fullName: String,
    val userid: String,
    val email: String,
    val roles: Set<Roles> = setOf()
) {
    fun isAdmin(): Boolean = roles.contains(Roles.Administrator)
}

data class EditableUser(
    var userid: String = "",
    var fullName: String = "",
    var email: String = "",
    var password: String = "",
    var roles: Set<Roles> = setOf()
) {
    fun validate(): Boolean {
        return fullName.isNotEmpty() && email.isNotEmpty() && roles.isNotEmpty()
    }
}

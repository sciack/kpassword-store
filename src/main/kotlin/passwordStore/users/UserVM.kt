package passwordStore.users

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import passwordStore.LOGGER
import passwordStore.utils.obfuscate

class UserVM(private val userRepository: UserRepository, private val coroutineScope: CoroutineScope) {

    val users = mutableStateListOf<ListUser>()

    val loggedUser = mutableStateOf<User>(NONE)

    val errorMsg = mutableStateOf("")

    suspend fun loadUsers() {
        withContext(Dispatchers.Main) {
            users.clear()
            errorMsg.value = ""
        }
        withContext(Dispatchers.IO) {
            users.addAll(userRepository.list())
        }

    }

    suspend fun delete(userid: String): Result<Unit> =
        withContext(Dispatchers.Main) {
            runCatching {
                withContext(Dispatchers.IO) {
                    errorMsg.value = ""
                }
                userRepository.deleteUser(userid)
            }.onSuccess {
                loadUsers()
            }.onFailure {
                LOGGER.warn(it) { "Error deleting user" }
                errorMsg.value = it.localizedMessage
            }
        }


    suspend fun createUser(user: EditableUser): Result<Unit> =
        withContext(Dispatchers.Main) {
            runCatching {
                errorMsg.value = ""
                withContext(Dispatchers.IO) {
                    userRepository.insertUser(user)
                }
            }.onSuccess {
                loadUsers()
            }.onFailure {
                LOGGER.warn(it) { "Error creating user" }
                errorMsg.value = it.localizedMessage
            }
        }

    suspend fun updateUser(newUser: EditableUser): Result<User> = withContext(Dispatchers.Main) {
        runCatching {
            errorMsg.value = ""
            withContext(Dispatchers.IO) {
                userRepository.updateUser(newUser)
            }
        }.onFailure {
            LOGGER.warn(it) {
                "Error finding user"
            }
            errorMsg.value = it.localizedMessage
        }
    }

    fun login(username: TextFieldValue, password: TextFieldValue): Result<User> {
        LOGGER.info {
            """Username: ${username.text}
        |Password: ${password.text.obfuscate()}
    """.trimMargin()
        }

        return runCatching {
            val user = userRepository.login(username.text, password.text)
            loggedUser.value = user
            user
        }.onFailure {
            passwordStore.LOGGER.warn(it) { "Wrong credentials" }
        }
    }

    fun isAdmin(): Boolean {
        return loggedUser.value.isAdmin()
    }

    companion object {
        val NONE =
            User(id = -1, userid = "", roles = setOf(), fullName = "Not logged in", email = "notLogged@example.com")
    }
}
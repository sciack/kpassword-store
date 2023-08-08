package passwordStore.users

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.kodein.di.DI
import java.security.Principal

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

        runCatching {
            errorMsg.value = ""
            userRepository.deleteUser(userid)
        }.onSuccess {
            loadUsers()
        }.onFailure {
            LOGGER.warn(it) { "Error deleting user" }
            errorMsg.value = it.localizedMessage
        }


    suspend fun createUser(user: EditableUser): Result<Unit> =
        runCatching {
            withContext(Dispatchers.Main) {
                errorMsg.value = ""
            }
            withContext(Dispatchers.IO) {
                userRepository.insertUser(user)
            }
        }.onSuccess {
            loadUsers()
        }.onFailure {
            LOGGER.warn(it) { "Error creating user" }
            errorMsg.value = it.localizedMessage
        }

    suspend fun findUser(userid: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            userRepository.findUser(userid)
        }.onFailure {
            LOGGER.warn(it) {
                "Error finding user"
            }
        }
    }

    suspend fun updateUser(newUser: EditableUser, principal: Principal): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            errorMsg.value = ""
            userRepository.updateUser(newUser, principal)
        }.onFailure {
            LOGGER.warn(it) {
                "Error finding user"
            }
            errorMsg.value = it.localizedMessage
        }
    }

    fun submit(di: DI, username: TextFieldValue, password: TextFieldValue): Result<User> {
        passwordStore.LOGGER.info {
            """Username: ${username.text}
        |Password: ${password.text}
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


    companion object {
        val LOGGER = KotlinLogging.logger {}
        val NONE =
            User(id = -1, userid = "", roles = setOf(), fullName = "Not logged in", email = "notLogged@example.com")
    }
}
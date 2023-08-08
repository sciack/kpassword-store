package passwordStore.users

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import mu.KotlinLogging
import passwordStore.utils.StatusHolder
import java.security.Principal

class UserVM(private val userRepository: UserRepository, private val coroutineScope: CoroutineScope) {

    val users = mutableStateListOf<ListUser>()

    val errorMsg = mutableStateOf("")

    suspend fun loadUsers() {

        users.clear()
        errorMsg.value = ""
        users.addAll(userRepository.list())

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
            errorMsg.value = ""
            userRepository.insertUser(user)
        }.onSuccess {
            loadUsers()
        }.onFailure {
            LOGGER.warn(it) { "Error creating user" }
            errorMsg.value = it.localizedMessage
        }

    suspend fun findUser(userid: String): Result<User> = runCatching {
        userRepository.findUser(userid)
    }.onFailure {
        LOGGER.warn(it) {
            "Error finding user"
        }
    }

    suspend fun updateUser(newUser: EditableUser, principal: Principal): Result<User> =
        runCatching {
            errorMsg.value = ""
            userRepository.updateUser(newUser, principal)
        }.onFailure {
            LOGGER.warn(it) {
                "Error finding user"
            }
            errorMsg.value = it.localizedMessage
        }


    companion object {
        val LOGGER = KotlinLogging.logger {}
    }
}
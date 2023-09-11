package passwordStore.users

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import passwordStore.LOGGER

class UserVM(private val userRepository: UserRepository) : ScreenModel {

    val users = mutableStateListOf<ListUser>()

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

    companion object {
        val NONE =
            User(id = -1, userid = "", roles = setOf(), fullName = "Not logged in", email = "notLogged@example.com")
    }
}


class CreateUserSM(private val userRepository: UserRepository) : ScreenModel {

    val errorMsg = mutableStateOf("")
    suspend fun createUser(user: EditableUser): Result<Unit> =
        withContext(Dispatchers.Main) {
            runCatching {
                errorMsg.value = ""
                withContext(Dispatchers.IO) {
                    userRepository.insertUser(user)
                }
            }.onFailure {
                LOGGER.warn(it) { "Error creating user" }
                errorMsg.value = it.localizedMessage
            }
        }
}
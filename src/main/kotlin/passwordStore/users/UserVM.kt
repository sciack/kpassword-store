package passwordStore.users

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

class UserVM(private val userRepository: UserRepository, private val coroutineScope: CoroutineScope) {

    val users = mutableStateListOf<ListUser>()

    val errorMsg = mutableStateOf("")

    suspend fun loadUsers() {

        users.clear()
        errorMsg.value = ""
        users.addAll(userRepository.list())

    }

    suspend fun delete(userid: String) {

        runCatching {
            userRepository.deleteUser(userid)
        }.onSuccess {
            loadUsers()
        }.onFailure {
            LOGGER.warn(it) { "Error deleting user" }
            errorMsg.value = it.localizedMessage
        }

    }

    suspend fun createUser(user: AddUser) {

        runCatching {
            userRepository.insertUser(user)
        }.onSuccess {
            loadUsers()
        }.onFailure {
            LOGGER.warn(it) { "Error creating user" }
            errorMsg.value = it.localizedMessage
        }

    }

    companion object {
        val LOGGER = KotlinLogging.logger {}
    }
}
package passwordStore.users

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.*

class UserVM(private val userRepository: UserRepository, private val coroutineScope: CoroutineScope) {

    val users = mutableStateListOf<ListUser>()

    fun loadUsers() {
        coroutineScope.launch(Dispatchers.IO) {
            users.clear()
            users.addAll(userRepository.list())
        }
    }

}
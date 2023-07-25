package passwordStore

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import passwordStore.audit.Event
import passwordStore.users.User

class Services(
    private val servicesRepository: ServicesRepository,
    private val scope: CoroutineScope
) {

    val services = mutableStateOf(listOf<Service>())

    val historyEvents = mutableStateOf(listOf<Event>())
    fun fetchAll(user: User) {
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user.userid)
            withContext(Dispatchers.Main) {
                services.value = result
            }
        }
    }

    suspend fun store(service: Service) {
        servicesRepository.store(service)
    }

    suspend fun update(service: Service) {
        servicesRepository.update(service)
    }

    suspend fun history(pattern: String, exactMatch: Boolean, user: User) {
        val result = servicesRepository.history(pattern, exactMatch, user)
        withContext(Dispatchers.Main) {
            historyEvents.value = result
        }
    }


}
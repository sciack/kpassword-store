package passwordStore

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import passwordStore.audit.Event
import passwordStore.tags.TagRepository
import passwordStore.users.User

class Services(
    private val servicesRepository: ServicesRepository,
    private val tagRepository: TagRepository,
    private val scope: CoroutineScope
) {

    val services = mutableStateOf(listOf<Service>())

    val historyEvents = mutableStateOf(listOf<Event>())

    val tags = mutableStateOf(mapOf<String, Int>())

    var user = User(id = -1, userid = "", roles = setOf(), fullName = "Not logged in", email = "notLogged@example.com")

    fun fetchAll() {
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user)
            val currentTags = tagRepository.tags(user)

            launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED ) {
                services.value = result
                tags.value = currentTags
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

    fun searchWithTags(tag: String) {
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user, tag= tag)
            withContext(Dispatchers.Main) {
                services.value = result
            }
        }
    }

    fun searchPattern(pattern: String) {
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern= pattern)
            withContext(Dispatchers.Main) {
                services.value = result
            }
        }
    }


}
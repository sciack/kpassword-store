package passwordStore.services

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

    val selectedService = mutableStateOf(Service())

    var user = NONE

    private var pattern: String = ""

    private var tag: String = ""

    fun fetchAll() {
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern, tag)
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
        fetchAll()
    }

    suspend fun history(pattern: String, exactMatch: Boolean, user: User) {
        val result = servicesRepository.history(pattern, exactMatch, user)
        withContext(Dispatchers.Main) {
            historyEvents.value = result
        }
    }

    fun searchWithTags(tag: String) {
        this.tag = tag
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern=pattern, tag= tag)
            withContext(Dispatchers.Main) {
                services.value = result
            }
        }
    }

    fun searchPattern(pattern: String) {
        this.pattern = pattern
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern= pattern, tag = tag)
            withContext(Dispatchers.Main) {
                services.value = result
            }
        }
    }

    fun selectService(service: Service) {
        selectedService.value = service
    }

    fun resetService() {
        selectedService.value = Service()
    }

    fun delete(service: Service) {
        scope.launch(Dispatchers.IO) {
            servicesRepository.delete(serviceName = service.service, userId = user.userid)
            fetchAll()
        }
    }

    companion object {
        val NONE = User(id = -1, userid = "", roles = setOf(), fullName = "Not logged in", email = "notLogged@example.com")
    }
}
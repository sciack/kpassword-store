package passwordStore.services

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import passwordStore.audit.Event
import passwordStore.tags.TagRepository
import passwordStore.users.User
import java.sql.SQLException

class ServiceViewModel(
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

    val saveError = mutableStateOf("")

    fun fetchAll() {
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern, tag)
            val currentTags = tagRepository.tags(user)

            launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED) {
                services.value = result
                tags.value = currentTags
                resetHistory()
            }
        }
    }

    suspend fun store(service: Service) = runCatching {
        servicesRepository.store(service)
    }.onSuccess {
        saveError.value = ""
    }.onFailure {
        saveError.value = processError(it, service)
    }

    private fun processError(exception: Throwable, service: Service): String {
        return when (exception) {
            is JdbcSQLIntegrityConstraintViolationException -> "Duplicate service: ${service.service}"
            is SQLException -> "Error storing the service ${service.service}: ${exception.localizedMessage}"
            else -> "Generic error storing ${service.service}"
        }
    }

    suspend fun update(service: Service) = runCatching {
        servicesRepository.update(service)
        fetchAll()
    }.onSuccess {
        saveError.value = ""
    }.onFailure {
        saveError.value = processError(it, service)
    }

    suspend fun history(pattern: String, exactMatch: Boolean) {
        val result = servicesRepository.history(pattern, exactMatch, user)
        withContext(Dispatchers.Main) {
            historyEvents.value = result
        }
    }

    fun searchWithTags(tag: String) {
        this.tag = tag
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern = pattern, tag = tag)
            withContext(Dispatchers.Main) {
                services.value = result
            }
        }
    }

    fun searchPattern(pattern: String) {
        this.pattern = pattern
        scope.launch(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern = pattern, tag = tag)
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

    fun resetHistory() {
        historyEvents.value = listOf()
    }

    fun shouldLoadHistory() = historyEvents.value.isEmpty()

    companion object {
        val NONE =
            User(id = -1, userid = "", roles = setOf(), fullName = "Not logged in", email = "notLogged@example.com")
    }
}
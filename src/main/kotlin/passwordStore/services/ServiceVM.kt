package passwordStore.services

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import passwordStore.LOGGER
import passwordStore.audit.Event
import passwordStore.tags.TagRepository
import passwordStore.users.UserVM
import java.sql.SQLException

class ServiceVM(
    private val servicesRepository: ServicesRepository,
    private val tagRepository: TagRepository,
    private val userVM: UserVM
) {

    val services = mutableStateOf(listOf<Service>())

    val historyEvents = mutableStateOf(listOf<Event>())

    val tags = mutableStateOf(mapOf<String, Int>())

    val selectedService = mutableStateOf(Service())

    private var pattern: String = ""

    private var tag: String = ""

    val saveError = mutableStateOf("")

    suspend fun fetchAll() {
        val user = userVM.loggedUser.value
        withContext(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern, tag)
            val currentTags = tagRepository.tags(user)
            withContext(Dispatchers.Main) {
                services.value = result
                tags.value = currentTags
                resetHistory()
            }

        }
    }

    suspend fun store(service: Service) = runCatching {
        servicesRepository.store(service.trim())
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
        servicesRepository.update(service.trim())
        fetchAll()
    }.onSuccess {
        resetService()
    }.onFailure {
        saveError.value = processError(it, service)
    }

    suspend fun history(pattern: String, exactMatch: Boolean) {
        val user = userVM.loggedUser.value
        val result = servicesRepository.history(pattern, exactMatch, user)
        withContext(Dispatchers.Main) {
            historyEvents.value = result
        }
    }

    suspend fun searchWithTags(tag: String) {
        this.tag = tag
        search()
    }

    private suspend fun search() {
        val user = userVM.loggedUser.value
        withContext(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern = pattern, tag = tag)
            withContext(Dispatchers.Main) {
                services.value = result
                saveError.value = ""
            }
        }
    }

    suspend fun searchPattern(pattern: String) {
        this.pattern = pattern
        search()
    }

    fun selectService(service: Service) {
        LOGGER.warn { "Set service $service" }
        selectedService.value = service
    }

    suspend fun resetService() {
        withContext(Dispatchers.Main) {
            selectedService.value = Service()
            saveError.value = ""
        }
    }

    suspend fun delete(service: Service) {
        val user = userVM.loggedUser.value
        withContext(Dispatchers.IO) {
            servicesRepository.delete(serviceName = service.service, userId = user.userid)
            fetchAll()
        }
    }

    fun resetHistory() {
        historyEvents.value = listOf()
    }

    fun shouldLoadHistory() = historyEvents.value.isEmpty()

    companion object {

    }
}
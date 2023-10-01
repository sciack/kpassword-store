package passwordStore.services

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import passwordStore.LOGGER
import passwordStore.services.ServiceSM.State.*
import passwordStore.services.audit.Event
import passwordStore.tags.Tag
import passwordStore.tags.TagElement
import passwordStore.tags.TagRepository
import passwordStore.users.User
import java.sql.SQLException


class ServicesSM(
    private val servicesRepository: ServicesRepository,
    private val tagRepository: TagRepository
) : StateScreenModel<ServicesSM.State>(State.Loading(setOf())) {

    sealed class State {
        data class Loading(val tags: TagElement) : State()
        data class Services(val services: List<Service>, val tags: TagElement) : State()
    }

    var pattern: String = ""

    var tag: Tag? = null

    private var lastTag: TagElement = setOf()

    fun resetSearch() {
        tag = null
        pattern = ""
    }

    suspend fun fetchAll(user: User) {
        resetSearch()
        mutableState.emit(State.Loading(lastTag))
        withContext(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern, tag?.name.orEmpty())
            val currentTags = tagRepository.tags(user)
            lastTag = currentTags
            //Thread.sleep(5000)
            withContext(Dispatchers.Main) {
                LOGGER.warn("Found ${result.size} records")
                mutableState.emit(State.Services(result, currentTags))

            }

        }
    }

    suspend fun searchWithTags(tag: String, user: User) {
        this.tag = Tag(tag)
        search(user)
    }

    private suspend fun search(user: User) {
        withContext(Dispatchers.IO) {
            mutableState.emit(State.Loading(lastTag))
            val result = servicesRepository.search(user, pattern = pattern, tag = tag?.name.orEmpty())
            val currentTags = tagRepository.tags(user)
            lastTag = currentTags
            //Thread.sleep(2000)
            withContext(Dispatchers.Main) {
                mutableState.emit(State.Services(result, currentTags))
            }
        }
    }

    suspend fun searchPattern(pattern: String, user: User) {
        this.pattern = pattern
        search(user)
    }


    suspend fun delete(service: Service, user: User) {
        withContext(Dispatchers.IO) {
            servicesRepository.delete(serviceName = service.service, userId = user.userid)
            fetchAll(user)
        }
    }

    companion object {

    }
}

class HistorySM(private val servicesRepository: ServicesRepository) : StateScreenModel<HistorySM.State>(State.First) {

    sealed interface State {
        data object First : State

        data class Loaded(val history: List<Event>) : State
    }

    val saveError = mutableStateOf("")

    suspend fun history(pattern: String, exactMatch: Boolean, user: User) {
        val result = servicesRepository.history(pattern, exactMatch, user)
        LOGGER.warn { "Found ${result.size} history events" }
        mutableState.emit(State.Loaded(result))
    }

    suspend fun store(service: Service) = runCatching {
        servicesRepository.store(service.trim())
    }.onSuccess {
        saveError.value = ""
    }.onFailure {
        saveError.value = processError(it, service)
    }

    fun loadHistory(service: String?, user: User) {
        coroutineScope.launch(Dispatchers.IO) {
            if (service.isNullOrEmpty()) {
                history("", false, user)
            } else {
                history(service, true, user)
            }
        }

    }
}

class CreateServiceSM(private val servicesRepository: ServicesRepository) : ScreenModel {
    val saveError = mutableStateOf("")

    suspend fun store(service: Service) = runCatching {
        servicesRepository.store(service.trim())
    }.onSuccess {
        saveError.value = ""
    }.onFailure {
        saveError.value = processError(it, service)
    }

}

private fun processError(exception: Throwable, service: Service): String {
    return when (exception) {
        is JdbcSQLIntegrityConstraintViolationException -> "Duplicate service: ${service.service}"
        is SQLException -> "Error storing the service ${service.service}: ${exception.localizedMessage}"
        else -> "Generic error storing ${service.service}"
    }
}


class ServiceSM(private val servicesRepository: ServicesRepository) :
    StateScreenModel<ServiceSM.State>(NoService) {
    sealed class State {
        data object NoService : State()
        data class EditService(val service: Service) : State()
        data class ShowService(val service: Service) : State()
    }

    val saveError = mutableStateOf("")

    fun showService(service: Service) {
        coroutineScope.launch {
            mutableState.emit(ShowService(service))
        }
    }

    fun editService(service: Service) {
        coroutineScope.launch {
            mutableState.emit(EditService(service))
        }
    }

    private fun hide() {
        coroutineScope.launch {
            mutableState.emit(NoService)
        }
    }

    suspend fun update(service: Service) = runCatching {
        servicesRepository.update(service.trim())

    }.onFailure {
        saveError.value = processError(it, service)
    }

    fun display(serviceDisplay: ServiceAction) {

        when (serviceDisplay) {
            is ServiceAction.Hide -> {
                hide()
            }

            is ServiceAction.Edit -> {
                editService(serviceDisplay.service)
            }

            is ServiceAction.Show -> {
                showService(serviceDisplay.service)
            }
        }
    }
}

sealed interface ServiceAction {

    data object Hide : ServiceAction

    data class Edit(val service: Service) : ServiceAction

    data class Show(val service: Service) : ServiceAction

}


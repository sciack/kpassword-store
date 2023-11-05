package passwordStore.services

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import kotlinx.coroutines.*
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import passwordStore.LOGGER
import passwordStore.services.ServiceSM.State.*
import passwordStore.services.audit.Event
import passwordStore.utils.EventBus
import passwordStore.utils.EventListener
import passwordStore.tags.Tag
import passwordStore.tags.TagElement
import passwordStore.tags.TagRepository
import passwordStore.users.User
import java.sql.SQLException
import kotlin.time.measureTimedValue


class ServicesSM(
    private val servicesRepository: ServicesRepository,
    private val tagRepository: TagRepository,
    private val eventBus: EventBus,
    coroutineScope: CoroutineScope
) : StateScreenModel<ServicesSM.State>(State.Loading(setOf())) {

    private data class SearchEvent(val user: User)

    sealed class State {
        data class Loading(val tags: TagElement) : State()
        data class Services(val services: List<Service>, val tags: TagElement) : State()
    }

    var pattern: MutableState<String> = mutableStateOf("")

    var tags: MutableState<Set<Tag>> = mutableStateOf(setOf())

    var suggestion: MutableList<Service> = mutableStateListOf()

    private var lastTags: TagElement = setOf()

    private val listener: EventListener<SearchEvent> = object : EventListener<SearchEvent> {
        override suspend fun onEvent(event: SearchEvent) {
            delay(200)
            search(event.user)
        }
    }

    init {
        eventBus.subscribe(listener)
    }

    fun resetSearch() {
        tags.value = setOf()
        pattern.value = ""

    }

    suspend fun fetchAll(user: User) {
        mutableState.emit(State.Loading(lastTags))
        eventBus.send(SearchEvent(user))
    }

    suspend fun searchWithTags(tags: Set<Tag>, user: User) {
        LOGGER.info {
            "Received tags $tags - current tags: ${this.tags}"
        }
        eventBus.send(SearchEvent(user))
    }

    private suspend fun search(user: User) {
        withContext(Dispatchers.IO + CoroutineName("Search")) {
            val currentSearch = pattern.value
            val currentTags = tags.value
            val (_, elapsed) = measureTimedValue {
                mutableState.emit(State.Loading(lastTags))
                val result = servicesRepository.search(user, currentSearch, currentTags.map { it.name }.toSet())
                val fetchedTags = tagRepository.tags(user)
                lastTags = fetchedTags
                withContext(Dispatchers.Main) {
                    mutableState.emit(State.Services(result, fetchedTags))
                }
            }
            LOGGER.info { "Fetch data $currentSearch & $currentTags took: $elapsed" }
        }
    }

    suspend fun searchPattern(pattern: String, user: User) {
        this.pattern.value = pattern
        eventBus.send(SearchEvent(user))
    }

    suspend fun searchFastPattern(pattern: String, user: User) {
        val result = servicesRepository.search(user, pattern, tags.value.map { it.name }.toSet())
        withContext(Dispatchers.Main) {
            suggestion.clear()
            suggestion.addAll(result.subList(0, 4.coerceAtMost(result.size)))
        }
    }

    suspend fun delete(service: Service, user: User) {
        withContext(Dispatchers.IO) {
            servicesRepository.delete(serviceName = service.service, userId = user.userid)
            fetchAll(user)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDispose() {
        super.onDispose()
        eventBus.unsubscribe(listener)
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


package passwordStore.services

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import org.apache.commons.csv.CSVFormat
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import passwordStore.LOGGER
import passwordStore.audit.Event
import passwordStore.tags.Tag
import passwordStore.tags.TagElement
import passwordStore.tags.TagRepository
import passwordStore.users.User
import java.nio.file.Path
import java.sql.SQLException
import kotlin.io.path.bufferedReader


class ServiceVM(
    private val servicesRepository: ServicesRepository,
    private val tagRepository: TagRepository
) {

    val services = mutableStateListOf<Service>()

    val tags = mutableStateListOf<Tag>()

    var pattern: String = ""

    var tag: Tag? = null

    val saveError = mutableStateOf("")

    fun resetSearch() {
        tag = null
        pattern = ""
    }

    suspend fun fetchAll(user: User) {
        resetSearch()
        withContext(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern, tag?.name.orEmpty())
            val currentTags = tagRepository.tags(user)
            withContext(Dispatchers.Main) {
                LOGGER.warn("Found ${result.size} records")
                services.clear()
                services.addAll(result)
                tags.clear()
                tags.addAll(currentTags)
            }

        }
    }

    suspend fun update(service: Service, user: User) = runCatching {
        servicesRepository.update(service.trim())
        fetchAll(user)
    }.onFailure {
        saveError.value = processError(it, service)
    }


    suspend fun searchWithTags(tag: String, user: User) {
        this.tag = Tag(tag)
        search(user)
    }

    private suspend fun search(user: User) {
        withContext(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern = pattern, tag = tag?.name.orEmpty())
            withContext(Dispatchers.Main) {
                services.clear()
                services.addAll(result)
                saveError.value = ""
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

    suspend fun readFile(path: Path, user: User): Result<Unit> {
        fun convert(tagString: String): List<String> {
            val tag = tagString.substringAfter('[').substringBeforeLast(']').split(',')
            return tag.map { it.trim() }.toList()
        }

        return withContext(Dispatchers.IO) {
            val csvFormat: CSVFormat = CSVFormat.EXCEL.builder().setHeader(*HEADERS).setSkipHeaderRecord(true).build()

            runCatching {
                path.bufferedReader().use {
                    csvFormat.parse(it).map { record ->
                        Service(
                            service = record[HEADERS[0]],
                            username = record[HEADERS[1]],
                            password = record[HEADERS[2]],
                            note = record[HEADERS[3]],
                            tags = convert(record[HEADERS[4]]),
                            updateTime = LocalDateTime.parse(record[HEADERS[5]]),
                            userid = user.userid
                        )
                    }.forEach { service ->
                        servicesRepository.store(service)
                    }
                }
            }.onSuccess {
                fetchAll(user)
            }.onFailure {
                LOGGER.warn(it) { "Error loading csv" }
            }
        }
    }


    companion object {
        private val HEADERS = arrayOf("Service", "Username", "Password", "Notes", "Tags", "Last Update")
    }
}

class HistorySM(private val servicesRepository: ServicesRepository) : ScreenModel {
    val historyEvents = mutableStateOf(listOf<Event>())

    val saveError = mutableStateOf("")

    suspend fun history(pattern: String, exactMatch: Boolean, user: User) {
        val result = servicesRepository.history(pattern, exactMatch, user)
        withContext(Dispatchers.Main) {
            historyEvents.value = result
        }
    }

    suspend fun store(service: Service) = runCatching {
        servicesRepository.store(service.trim())
    }.onSuccess {
        saveError.value = ""
    }.onFailure {
        saveError.value = processError(it, service)
    }

    fun resetHistory() {
        historyEvents.value = listOf()
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

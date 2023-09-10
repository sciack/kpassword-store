package passwordStore.services

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import org.apache.commons.csv.CSVFormat
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import passwordStore.LOGGER
import passwordStore.audit.Event
import passwordStore.tags.TagRepository
import passwordStore.users.UserVM
import java.nio.file.Path
import java.sql.SQLException
import kotlin.io.path.bufferedReader


class ServiceVM(
    private val servicesRepository: ServicesRepository,
    private val tagRepository: TagRepository,
    private val userVM: UserVM
) {

    val services = mutableStateListOf<Service>()

    val historyEvents = mutableStateOf(listOf<Event>())

    val tags = mutableStateOf(mapOf<String, Int>())

    private var pattern: String = ""

    private var tag: String = ""

    val saveError = mutableStateOf("")

    suspend fun fetchAll() {
        val user = userVM.loggedUser.value
        withContext(Dispatchers.IO) {
            val result = servicesRepository.search(user, pattern, tag)
            val currentTags = tagRepository.tags(user)
            withContext(Dispatchers.Main) {
                LOGGER.warn("Found ${result.size} records")
                services.clear()
                services.addAll(result)
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
                services.clear()
                services.addAll(result)
                saveError.value = ""
            }
        }
    }

    suspend fun searchPattern(pattern: String) {
        this.pattern = pattern
        search()
    }


    suspend fun delete(service: Service) {
        val user = userVM.loggedUser.value
        withContext(Dispatchers.IO) {
            servicesRepository.delete(serviceName = service.service, userId = user.userid)
            fetchAll()
        }
    }

    private fun resetHistory() {
        historyEvents.value = listOf()
    }

    fun shouldLoadHistory() = historyEvents.value.isEmpty()
    suspend fun readFile(path: Path): Result<Unit> {
        fun convert(tagString: String): List<String> {
            val tag = tagString.substringAfter('[').substringBeforeLast(']').split(',')
            return tag.map { it.trim() }.toList()
        }

        return withContext(Dispatchers.IO) {
            val csvFormat: CSVFormat = CSVFormat.EXCEL.builder()
                .setHeader(*HEADERS)
                .setSkipHeaderRecord(true)
                .build()

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
                            userid = userVM.loggedUser.value.userid
                        )
                    }.forEach { service ->
                        servicesRepository.store(service)
                    }
                }
            }.onSuccess {
                fetchAll()
            }.onFailure {
                LOGGER.warn(it) { "Error loading csv" }
            }
        }
    }


    companion object {
        private val HEADERS = arrayOf("Service", "Username", "Password", "Notes", "Tags", "Last Update")
    }
}
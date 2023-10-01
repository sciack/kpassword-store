package passwordStore.services

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import org.apache.commons.csv.CSVFormat
import passwordStore.LOGGER
import passwordStore.users.User
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder
import kotlin.io.path.bufferedReader
import kotlin.io.path.writer

fun exportPath(): Path {
    val path = Path.of(System.getProperty("user.home"))
    return Path.of(path.toString(), "Downloads", "services.csv")
}

private val HEADERS = arrayOf("Service", "Username", "Password", "Notes", "Tags", "Url", "Last Update")

class ExportSM(private val servicesRepository: ServicesRepository) : StateScreenModel<ExportSM.State>(State.Export) {

    sealed class State {
        data object Export : State()
        data class Exporting(val rows: Int, val totalRow: Int) : State()
        data class Exported(val csvFile: Path) : State()
        data class Error(val errorMsg: String) : State()
    }

    fun startExport(path: Path, user: User) {
        coroutineScope.launch {
            performDownload(path, user)
        }
    }

    suspend fun performDownload(csvFile: Path, user: User) {
        runCatching {
            val services = servicesRepository.search(user)
            mutableState.emit(State.Exporting(0, services.size))

            withContext(Dispatchers.IO) {
                runCatching {
                    csvFile.writer().use { writer ->
                        CSVFormat.EXCEL.print(writer).apply {
                            printRecord(*HEADERS)
                            services.forEachIndexed { index, service ->
                                printRecord(
                                    service.service,
                                    service.username,
                                    service.password,
                                    service.note,
                                    service.tags,
                                    service.url,
                                    service.updateTime
                                )
                                mutableState.emit(State.Exporting(index + 1, services.size))
                                Thread.sleep(100)
                            }
                        }
                    }
                }.onSuccess {
                    mutableState.emit(State.Exported(csvFile))
                }.getOrThrow()
            }
        }.onFailure {
            mutableState.emit(State.Error("Error performing export: ${it.localizedMessage}"))
        }
    }
}


class ImportSM(private val servicesRepository: ServicesRepository) : StateScreenModel<ImportSM.State>(State.Import) {

    sealed class State {
        data object Import : State()
        data class Loading(val correct: Int, val reject: Int, val currentRecord: Int, val totalRecord: Int) : State()

        data class Loaded(val csvFile: Path) : State()

        data class Error(val errorMsg: String) : State()
    }

    fun startLoading(path: Path, user: User) {
        coroutineScope.launch {
            readFile(path, user)
        }
    }

    suspend fun readFile(csvFile: Path, user: User): Result<Unit> {
        fun convert(tagString: String): Set<String> {
            val tag = tagString.substringAfter('[').substringBeforeLast(']').split(',')
            return tag.map { it.trim() }.toSet()
        }

        return withContext(Dispatchers.IO) {
            val csvFormat: CSVFormat =
                CSVFormat.EXCEL.builder().setHeader(*HEADERS).setSkipHeaderRecord(true).build()
            val successCounter = LongAdder()
            val failCounter = LongAdder()
            val recordNumber = AtomicInteger(0)
            runCatching {
                csvFile.bufferedReader().use { reader ->
                    csvFormat.parse(reader).records.apply {
                        recordNumber.set(this.size.let { if (it == 0) 1 else it })
                        mutableState.emit(State.Loading(0, 0, 0, recordNumber.get()))
                    }.asSequence().map { record ->
                        Service(
                            service = record[HEADERS[0]],
                            username = record[HEADERS[1]],
                            password = record[HEADERS[2]],
                            note = record[HEADERS[3]],
                            tags = convert(record[HEADERS[4]]),
                            url = record[HEADERS[5]],
                            updateTime = LocalDateTime.parse(record[HEADERS[6]]),
                            userid = user.userid
                        )
                    }.forEachIndexed { index, service ->
                        runCatching {
                            servicesRepository.store(service)
                        }.onSuccess {
                            successCounter.add(1)
                        }.onFailure {
                            failCounter.add(1)
                        }
                        mutableState.emit(
                            State.Loading(
                                successCounter.toInt(),
                                failCounter.toInt(),
                                index + 1,
                                totalRecord = recordNumber.toInt()
                            )
                        )
                        Thread.sleep(100) //force slow down, so UI can see the data
                    }
                }
            }.onSuccess {
                mutableState.emit(State.Loaded(csvFile))
            }.onFailure {
                LOGGER.warn(it) { "Error loading csv" }
                mutableState.emit(State.Error("Error performing export: ${it.localizedMessage}"))
            }

        }
    }
}
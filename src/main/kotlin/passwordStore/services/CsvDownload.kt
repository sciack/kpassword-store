package passwordStore.services

import org.apache.commons.csv.CSVFormat
import org.kodein.di.DI
import org.kodein.di.instance
import passwordStore.users.UserVM
import java.io.Writer
import java.nio.file.Path

suspend fun Writer.performDownload(di: DI) {
    val serviceRepository by di.instance<ServicesRepository>()
    val userVM by di.instance<UserVM>()
    val services = serviceRepository.search(userVM.loggedUser.value)

    CSVFormat.EXCEL.print(this@performDownload).apply {
        printRecord("Service", "Username", "Password", "Notes", "Tags", "Last Update")
        services.forEach { service ->
            printRecord(
                service.service,
                service.username,
                service.password,
                service.note,
                service.tags,
                service.updateTime
            )
        }
    }
}

fun exportPath(): Path {
    val path = Path.of(System.getProperty("user.home"))
    return Path.of(path.toString(), "Downloads", "services.csv")
}
package passwordStore.config

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import passwordStore.crypto.IV_STRING
import passwordStore.crypto.SECRET_KEY
import passwordStore.crypto.Secrets
import passwordStore.sql.DB_PASSWORD
import passwordStore.sql.JDBC_URL
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.reader
import kotlin.io.path.writer

class ConfigVM(val configFile: Path, secrets: Secrets, dataSource: HikariDataSource) {


    var jdbcUrl: String
    var dbPassword: String
    var secret: String
    var ivSpec: String

    private val properties: Properties = Properties()

    init {
        configFile.reader().use {
            properties.load(it)
        }
        jdbcUrl = properties.getProperty(JDBC_URL, dataSource.jdbcUrl)
        dbPassword = properties.getProperty(DB_PASSWORD, "default")
        secret = String(
            Base64.decodeBase64(
                properties.getProperty(
                    SECRET_KEY,
                    Base64.encodeBase64String(secrets.passphrase())
                )
            )
        )
        ivSpec = String(
            Base64.decodeBase64(
                properties.getProperty(
                    IV_STRING,
                    Base64.encodeBase64String(secrets.ivString())
                )
            )
        )
    }


    suspend fun save() {
        properties.setProperty(JDBC_URL, jdbcUrl)
        properties.setProperty(DB_PASSWORD, dbPassword)
        properties.setProperty(SECRET_KEY, Base64.encodeBase64String(secret.toByteArray()))
        properties.setProperty(IV_STRING, Base64.encodeBase64String(ivSpec.toByteArray()))
        withContext(Dispatchers.IO) {
            LOGGER.warn { "Saving properties in file ${configFile.absolutePathString()}" }
            configFile.writer().use {
                properties.store(it, "Generated by Password Store save")
            }
        }
    }

    companion object {
        val LOGGER = KotlinLogging.logger { }
    }
}
package passwordStore.config

import mu.KotlinLogging
import passwordStore.LOGGER
import passwordStore.utils.Platform
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.reader

object SetupEnv {
    private val LOGGER = KotlinLogging.logger { }
    fun configure(path: Path) {
        LOGGER.info { "Read configuration from ${path.absolutePathString()}" }
        val properties = Properties()
        path.reader().use {
            properties.load(it)
        }
        properties.keys.forEach { k ->
            val key = k.toString()
            if (System.getProperty(key).isNullOrEmpty()) {
                System.setProperty(key, properties.getProperty(key))
            } else {
                LOGGER.info { "Property $key already set, avoid override" }
            }
        }
    }

}

fun configureEnvironment() {
    val mode = getMode()

    val configFile = if (mode == "PROD") {
        val dir = configDir()
        dir.createDirectories()
        dir.resolve("config.properties").also { path ->
            if (path.notExists()) {
                val template =
                    Thread.currentThread().contextClassLoader.getResource("/config.properties.template")?.readText()
                        .orEmpty()
                Files.writeString(path, template)
            }
        }
    } else {
        Path.of(".env")
    }
    LOGGER.info { "Reading configuration for file ${configFile.toAbsolutePath()}" }
    SetupEnv.configure(configFile)
}

fun getMode(): String = System.getProperty("kpassword-store.mode", "TEST")

fun configDir(): Path {
    val os = Platform.os()
    val userHome = System.getProperty("user.home")
    return os.configDirectory(userHome)
}

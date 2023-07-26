package passwordStore.config

import mu.KotlinLogging
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.absolutePathString
import kotlin.io.path.reader

object SetupEnv {
    private val LOGGER = KotlinLogging.logger {  }
    fun configure(envFile: String) {
        val path = Path.of(envFile)
        LOGGER.info {"Read configuration from ${path.absolutePathString()}"}
        val properties = Properties()
        path.reader().use {
            properties.load(it)
        }
        properties.keys.forEach { k ->
            val key = k.toString()
            System.setProperty(key, properties.getProperty(key))
        }
    }

}
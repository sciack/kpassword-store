package passwordStore.config

import mu.KotlinLogging
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolutePathString
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
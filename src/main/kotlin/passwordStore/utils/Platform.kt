package passwordStore.utils

import mu.KLogger
import mu.KotlinLogging
import org.slf4j.bridge.SLF4JBridgeHandler
import java.nio.file.Path

object Platform {
    enum class OsFamily {
        WINDOWS,
        LINUX,
        MAC,
        OTHER;

        fun configDirectory(userHome: String): Path =
            when (this) {
                WINDOWS -> Path.of("$userHome/AppData/Local/KPasswordStore")
                else -> Path.of("$userHome/.config/KPasswordStore")
            }

    }


    fun os(): OsFamily {
        val osName = System.getProperty("os.name").lowercase()

        return when {
            osName == "linux" -> OsFamily.LINUX
            osName == "macos" -> OsFamily.MAC
            osName.startsWith("win") -> OsFamily.WINDOWS
            else -> OsFamily.OTHER
        }
    }

}

@Suppress("NOTHING_TO_INLINE")
inline fun logger(): KLogger = KotlinLogging.logger {}

fun configureLog(): KLogger {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    return logger()
}
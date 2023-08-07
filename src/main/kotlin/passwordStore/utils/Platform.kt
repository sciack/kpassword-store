package passwordStore.utils

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
package passwordStore.crypto

import org.apache.commons.codec.binary.Base64


interface Secrets {
    fun passphrase(): ByteArray
}

private class EnvSecrets() : Secrets {
    override fun passphrase(): ByteArray {
        val passfrase = System.getenv("passphrase")
        return Base64.decodeBase64(passfrase)
    }
}

private class DevSecrets() : Secrets {

    override fun passphrase(): ByteArray {
        return "DEFAULT PASSPHRASE".toByteArray()
    }
}

class SecretsFactory(private val runtime: String = "") {


    fun secrets(): Secrets {
        return when (runtime.lowercase()) {
            "docker" -> EnvSecrets()
            else -> DevSecrets()
        }
    }
}

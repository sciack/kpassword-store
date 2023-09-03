package passwordStore.crypto

import org.apache.commons.codec.binary.Base64


interface Secrets {
    fun passphrase(): ByteArray
}

private class PropsSecret : Secrets {
    private val passphrase by lazy {
        System.getProperty(SECRET_KEY)
    }

    override fun passphrase(): ByteArray {
        return Base64.decodeBase64(passphrase)
    }
}

private class DevSecrets : Secrets {

    override fun passphrase(): ByteArray {
        return "DEFAULT PASSPHRASE".toByteArray()
    }
}

class SecretsFactory(private val runtime: String = "") {

    fun secrets(): Secrets {
        return when (runtime.lowercase()) {
            "prod" -> PropsSecret()
            else -> DevSecrets()
        }
    }
}

const val SECRET_KEY="secret"

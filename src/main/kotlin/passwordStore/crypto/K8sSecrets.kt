package passwordStore.crypto

import org.apache.commons.codec.binary.Base64


interface Secrets {
    fun passphrase(): ByteArray

    fun ivString(): ByteArray
}

private class PropsSecret : Secrets {
    private val passphrase by lazy {
        System.getProperty(SECRET_KEY)
    }

    private val ivString by lazy {
        System.getProperty(IV_STRING, System.getProperty(SECRET_KEY))
    }

    override fun passphrase(): ByteArray {
        return Base64.decodeBase64(passphrase)
    }

    override fun ivString(): ByteArray {
        return  Base64.decodeBase64(ivString).slice(0 until 16).toByteArray()
    }
}

private class DevSecrets : Secrets {

    override fun passphrase(): ByteArray {
        return "DEFAULT PASSPHRASE".toByteArray()
    }

    override fun ivString(): ByteArray {
        return "DEFAULT IV String for development".toByteArray().slice(0 until 16).toByteArray()
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
const val IV_STRING ="IV_STRING"
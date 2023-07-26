package passwordStore.crypto

import org.apache.commons.codec.binary.Base64


interface Secrets {
    fun passphrase(): ByteArray
}

private class PropsSecret : Secrets {
    private val passfrase by lazy {
        System.getProperty("secret")
    }

    override fun passphrase(): ByteArray {
        return Base64.decodeBase64(passfrase)
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

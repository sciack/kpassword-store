/*
 *
 * MIT License
 *
 * Copyright (c) 2020 Mirko Sciachero
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package passwordStore.crypto

import mu.KotlinLogging
import org.apache.commons.codec.binary.Hex
import org.jasypt.util.password.StrongPasswordEncryptor
import org.kodein.di.*
import java.security.MessageDigest
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class CryptExtension(private var secrets: Secrets) {
    companion object {
        private val LOGGER = KotlinLogging.logger { }
        private val encryptor = StrongPasswordEncryptor()

        private const val ALGORITHM = "AES/GCM/NoPadding"
        fun String.hash(): String = encryptor.encryptPassword(this)
        fun String.verify(password: String): Boolean = encryptor.checkPassword(password, this)

    }


    private val key: SecretKeySpec by lazy {
        val digest =
            MessageDigest.getInstance("SHA-256") //should move to SHA-2 but implementing convertion could be disruptive
        val passphrase = runCatching {
            secrets.passphrase()
        }.onFailure {
            LOGGER.warn("Error retrieving the secrets", it)
        }.getOrThrow()
        digest.update(passphrase)
        SecretKeySpec(digest.digest(), 0, 16, "AES")
    }

    private val paramSpec: AlgorithmParameterSpec by lazy {
        GCMParameterSpec(128, secrets.ivString())
    }

    fun decrypt(string: String) =
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, paramSpec)
            LOGGER.debug {"Using algorithm: ${cipher} - ${cipher.algorithm}"}
            String(cipher.doFinal(Hex.decodeHex(string.toCharArray())))
        } catch (e: Exception) {
            LOGGER.warn("Unable to decrypt instance: $e", e)
            string
        }

    fun crypt(string: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec)
        return Hex.encodeHexString(cipher.doFinal(string.toByteArray()))
    }
}


val prodCryptExtension = DI.Module("prodCryptoExtension") {
    bind<Secrets> {
        provider {
            SecretsFactory("prod").secrets()
        }
    }
    bind<CryptExtension> {
        singleton {
            CryptExtension(instance())
        }
    }
}

val devCryptExtension = DI.Module("devCryptoExtension") {
    bind<Secrets> {
        provider {
            SecretsFactory("dev").secrets()
        }
    }
    bind<CryptExtension> {
        singleton {
            CryptExtension(instance())
        }
    }
}
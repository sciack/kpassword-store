package passwordStore.crypto

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class CryptExtensionTest {

    private lateinit var crypto: CryptExtension

    @BeforeTest
    fun setup() {
        val secrets = mockk<Secrets>()
        crypto = CryptExtension(secrets)
        every { secrets.passphrase() } returns "TestPassphrase".toByteArray()
        every { secrets.ivString() } returns "IV String if 16B".toByteArray().slice(0 until 16).toByteArray()

    }

    @Test
    fun `should be able to decrypt a previous encrypted message`() {
        val message = "This is a message to encrypt"
        assertThat(crypto.decrypt(crypto.crypt(message)), equalTo(message))
    }


    @Test
    fun `should be able to crypt the message`() {
        val message = "This is a message to encrypt"
        assertThat(crypto.crypt(message), equalTo(message).not())
    }
}
package passwordStore.widget

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlin.test.Test


class DialogsKtTest {

    @Test
    fun `should generate a password with the right length`() {
        val pwd = generatePassword(16, true, true, true)
        print(pwd)
        assertThat(pwd.length, equalTo(16))
    }
}
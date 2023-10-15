package passwordStore.widget

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test

@RunWith(value= Parameterized::class)
class DialogsKtTest(private val length: Int) {

    @Test
    fun `should generate a password with the right length`() {
        val pwd = generatePassword(length, true, true, true)
        assertThat(pwd.length, equalTo(length))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: PasswordLength({0})")
        fun data(): Iterable<Array<Int>> {
            return (8..16).map {arrayOf(it)}
        }
    }
}
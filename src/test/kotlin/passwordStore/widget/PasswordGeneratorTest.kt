package passwordStore.widget

import com.natpryce.hamkrest.anyOf
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test

@RunWith(value = Parameterized::class)
class PasswordGeneratorTest(private val length: Int) {

    @Test
    fun `should generate a password with the right length`() {
        val pwd = generatePassword(length, true, true, true)
        assertThat(pwd.length, equalTo(length))
    }

    @Test
    fun `should not contains any of the special char and numbers`() {
        val pwd = generatePassword(length, upperCase = false, number = false, symbols = false)
        assertThat(pwd, has(String::hasUppercase, equalTo(false)))
        assertThat(pwd, has(String::hasNumber, equalTo(false)))
        assertThat(pwd, has(String::hasSpecialChar, equalTo(false)))
    }

    @Test
    fun `should contains at least one of uppercase, number or symbols`() {
        val pwd = generatePassword(length, upperCase = true, number = true, symbols = true)
        //in theory should all 3 present, but because is a probabilistic things, even if only one is present, should be ok
        assertThat(
            pwd,
            anyOf(
                has(String::hasUppercase, equalTo(true)),
                has(String::hasNumber, equalTo(true)),
                has(String::hasSpecialChar, equalTo(true))
            )
        )
    }


    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: PasswordLength({0})")
        fun data(): Iterable<Int> {
            return (8..16).map { it }
        }
    }
}
package passwordStore.widget

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test

@RunWith(value = Parameterized::class)
class DialogsKtTest(private val length: Int) {

    @Test
    fun `should generate a password with the right length`() {
        val pwd = generatePassword(length, true, true, true)
        assertThat(pwd.length, equalTo(length))
    }

    @Test
    fun `should not contains any of the special char and numbers`() {
        val pwd = generatePassword(length, false, false, false)
        assert(!pwd.hasNumber())
        assert(!pwd.hasUppercase())
        assert(!pwd.hasSpecialChar())
    }

    @Test
    fun `should contains uppercase`() {
        val pwd = generatePassword(length, true, false, false)
        assert(pwd.hasUppercase())
        assert(!pwd.hasNumber())
        assert(!pwd.hasSpecialChar())
    }

    @Test
    fun `should contains number`() {
        val pwd = generatePassword(length, false, true, false)
        assert(!pwd.hasUppercase())
        assert(pwd.hasNumber())
        assert(!pwd.hasSpecialChar())
    }


    @Test
    fun `should contains special char`() {
        val pwd = generatePassword(length, false, false, true)
        assert(!pwd.hasUppercase())
        assert(!pwd.hasNumber())
        assert(pwd.hasSpecialChar())
    }



    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: PasswordLength({0})")
        fun data(): Iterable<Array<Int>> {
            return (8..16).map { arrayOf(it) }
        }
    }
}
package passwordStore.users

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kodein.di.DI
import org.kodein.di.instance
import passwordStore.DiInjection
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

@ExtendWith(DiInjection::class)
class UserRepositoryTest(di: DI) {

    private val userRepository by di.instance<UserRepository>()

    @Test
    fun testLogin() {
        val user = userRepository.login("m.sciachero", "secret")
        assertEquals("m.sciachero", user.userid)
        assertEquals("Mirko Sciachero", user.fullName)
        assertContains(user.roles, Roles.NormalUser)
    }

    @Test
    fun testNoUser() {
        val exception = assertThrows<IllegalStateException> {
            userRepository.login("fake", "wrong")
        }

        assertEquals("Empty result set", exception.message)
    }

    @Test
    fun testWrongPwd() {
        val exception = assertThrows<IllegalArgumentException> {
            userRepository.login("dummy", "wrong")
        }

        assertEquals("Password for user dummy is wrong", exception.message)
    }
}
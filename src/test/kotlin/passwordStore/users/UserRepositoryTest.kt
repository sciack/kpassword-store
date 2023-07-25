package passwordStore.users

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.kodein.di.instance
import passwordStore.DiInjection
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class UserRepositoryTest() {
    private val di = DiInjection.testDi
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
        assertThat({
            userRepository.login("fake", "wrong")
        }, throws<IllegalStateException>(MatchMessage(equalTo("Empty result set"))))
    }

    @Test
    fun testWrongPwd() {
        assertThat({
            userRepository.login("dummy", "wrong")
        }, throws<IllegalArgumentException>(MatchMessage(equalTo("Password for user dummy is wrong"))))

    }
}

class MatchMessage(private val matcher: Matcher<String?>) : Matcher<Throwable> {

    override val description: String
        get() = "message match ${matcher.description}"

    override fun invoke(actual: Throwable): MatchResult {
        return matcher(actual.message)
    }
}
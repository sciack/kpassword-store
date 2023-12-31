package passwordStore.users

import com.github.javafaker.Faker
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.kodein.di.instance
import passwordStore.DiInjection
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class UserRepositoryTest() {
    private val di = DiInjection.testDi
    private val userRepository by di.instance<UserRepository>()

    @AfterTest
    fun tearDown() {
        val user = userRepository.findUser("dummy")
        val resetUser = EditableUser(
            userid = user.userid,
            email = "dummy@example.com",
            fullName = "dummpy",
            password = "secret"
        )
        userRepository.updateUser(resetUser)
    }

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

    @Test
    fun `should not change password if is empty`() {
        val user = userRepository.login("dummy", "secret").let {
            EditableUser(
                fullName = it.fullName,
                password = "",
                email = it.email,
                userid = it.userid
            )
        }
        userRepository.updateUser(user)
        userRepository.login("dummy", "secret")

    }

    @Test
    fun `should store a new user`() {
        val faker = Faker()
        val user = EditableUser(
            fullName = faker.dune().character(),
            password = faker.internet().password(),
            email = faker.internet().emailAddress(),
            roles = setOf(Roles.NormalUser),
            userid = userRepository.login("dummy", "secret").userid
        )
        userRepository.updateUser(user)
        val userRead = userRepository.login(user.userid, user.password)
        assertThat(userRead.fullName, equalTo(user.fullName))
        assertThat(userRead.email, equalTo(user.email))
        assertThat(userRead.roles, equalTo(setOf(Roles.NormalUser)))
    }
}

class MatchMessage(private val matcher: Matcher<String?>) : Matcher<Throwable> {

    override val description: String
        get() = "message match ${matcher.description}"

    override fun invoke(actual: Throwable): MatchResult {
        return matcher(actual.message)
    }
}
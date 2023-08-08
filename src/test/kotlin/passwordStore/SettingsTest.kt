package passwordStore

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.github.javafaker.Faker
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await
import org.junit.Rule
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.users.EditableUser
import passwordStore.users.User
import passwordStore.users.UserRepository
import passwordStore.users.userSettings
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class SettingsTest {
    private val di = DiInjection.testDi
    private val faker = Faker()
    private lateinit var user: User
    private val userRepository by di.instance<UserRepository>()

    @get:Rule
    val rule = createComposeRule()

    @BeforeTest
    fun setup() {
        user = userRepository.login("dummy", "secret")
    }


    @AfterTest
    fun tearDown() {
        val user = userRepository.findUser("dummy")
        val resetUser = EditableUser(
            userid = user.userid,
            email = "dummy@example.com",
            fullName = "dummy",
            password = "secret"
        )
        userRepository.updateUser(resetUser, user.asPrincipal())
    }

    @Test
    fun `should store the new settings`() = runTest {
        rule.setContent {
            withDI(di) {
                userSettings(user)
            }
        }

        rule.awaitIdle()
        val fullName = faker.dune().character()
        rule.onNodeWithTag("fullName").assertExists().performTextReplacement(fullName)
        val email = faker.internet().emailAddress()
        rule.onNodeWithTag("email").assertExists().performTextReplacement(email)
        val password = faker.internet().password()
        rule.onNodeWithTag("password").assertExists().performTextReplacement(password)
        rule.onNodeWithTag("password-confirmation").assertExists().performTextReplacement(password)

        rule.onNodeWithTag("submit").assertExists().performClick()

        await.atMost(1.seconds.toJavaDuration()).untilAsserted {
            val changedUser = userRepository.login("dummy", password)
            assertThat(changedUser.email, equalTo(email))
        }
    }

    @Test
    fun `should submitted disable if password is not matching`() = runTest {
        rule.setContent {
            withDI(di) {
                userSettings(user)
            }
        }

        rule.awaitIdle()
        val fullName = faker.dune().character()
        rule.onNodeWithTag("fullName").assertExists().performTextReplacement(fullName)
        val email = faker.internet().emailAddress()
        rule.onNodeWithTag("email").assertExists().performTextReplacement(email)
        val password = faker.internet().password()
        rule.onNodeWithTag("password").assertExists().performTextReplacement(password)

        rule.onNodeWithTag("submit").assertExists().performClick()

    }
}
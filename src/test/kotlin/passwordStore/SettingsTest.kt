package passwordStore

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.javafaker.Faker
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Rule
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.crypto.CryptExtension.Companion.hash
import passwordStore.crypto.CryptExtension.Companion.verify
import passwordStore.navigation.NavController
import passwordStore.services.ServiceViewModel
import passwordStore.services.ServicesRepository
import passwordStore.users.UpdateUser
import passwordStore.users.User
import passwordStore.users.UserRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SettingsTest {
    private val di = DiInjection.testDi
    private val clock: Clock by di.instance()
    private val servicesRepository by di.instance<ServicesRepository>()
    private val serviceModel by di.instance<ServiceViewModel>()
    private val navController by di.instance<NavController>()
    private val faker = Faker()
    private lateinit var user:User
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
        val resetUser = UpdateUser(
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
                settings(user)
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

        val changedUser = userRepository.login("dummy", password)

        assertThat(changedUser.email, equalTo(email))
    }

    @Test
    fun `should submitted disable if password is not matching`() = runTest {
        rule.setContent {
            withDI(di) {
                settings(user)
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
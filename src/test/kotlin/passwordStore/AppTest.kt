package passwordStore

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.awaitility.kotlin.await
import org.junit.Rule
import org.kodein.di.instance
import passwordStore.navigation.NavController
import passwordStore.services.Service
import passwordStore.services.ServiceViewModel
import passwordStore.services.ServiceViewModel.Companion.NONE
import passwordStore.services.ServicesRepository
import passwordStore.utils.currentTime
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppTest {
    private val di = DiInjection.testDi
    private val user = testUser()
    private val clock: Clock by di.instance()
    private val servicesRepository by di.instance<ServicesRepository>()
    private val serviceModel by di.instance<ServiceViewModel>()

    @get:Rule
    val rule = createComposeRule()

    @BeforeTest
    fun setUp() {
        serviceModel.user = NONE
        val navController by di.instance<NavController>()
        navController.currentScreen.value = Screen.Login
    }

    @AfterTest
    fun tearDown() {
        runBlocking(Dispatchers.IO) {
            servicesRepository.search(serviceModel.user).forEach {
                servicesRepository.delete(it.service, it.userid)
            }
        }
        serviceModel.user = NONE

    }

    @Test
    fun shouldShowLogin() = runTest {
        rule.setContent {
            App(di)
        }

        performLogin()
        rule.awaitIdle()
        rule.onNodeWithTag("Search field").assertExists()
    }

    @Test
    fun shouldShowAnErrorIfLoginFail() = runTest {
        rule.setContent {
            App(di)
        }

        performLogin("wrong user")
        rule.awaitIdle()
        rule.onNodeWithTag("Login error msg").assertExists().assertTextContains("Invalid credentials")
    }


    @Test
    fun `should be able to add a service`() = runTest {
        rule.setContent {
            App(di)
        }
        rule.awaitIdle()
        performLogin()
        val service = Service(
            service = "myService",
            username = "a username",
            password = "a password",
            tags = listOf("tag"),
            note = "someNote",
            userid = user.userid,
            dirty = true,
            score = 0.0,
            updateTime = clock.currentTime()
        )
        rule.awaitIdle()
        rule.onNodeWithTag("Search field").assertExists()

        insertService(service)
        rule.awaitIdle()
        rule.onNodeWithTag("Search field").assertExists()
        rule.waitUntilNodeCount(hasText(service.service), 1, 3000)

    }

    private suspend fun insertService(service: Service) {
        rule.onNodeWithTag("Drawer").performClick()
        rule.waitUntilExactlyOneExists(hasTestTag("New Service"))
        rule.onNodeWithTag("New Service").performClick()
        rule.waitUntilExactlyOneExists(hasTestTag("service"))

        rule.onNodeWithTag("service").performTextInput(service.service)
        rule.onNodeWithTag("username").performTextInput(service.username)
        rule.onNodeWithTag("password").performTextInput(service.password)
        rule.onNodeWithTag("tags").performTextInput(service.tags[0])
        rule.onNodeWithTag("note").performTextInput(service.note)
        rule.awaitIdle()

        rule.onNodeWithTag("submit").performClick()
        rule.waitUntil(timeoutMillis = 1000) {
            runBlocking {
                servicesRepository.search(serviceModel.user, "", "").any {
                    it.service == service.service
                }
            }
        }

        rule.waitUntilAtLeastOneExists(hasTestTag("Search field"))
    }

    private suspend fun performLogin(username: String = "m.sciachero") {
        rule.awaitIdle()
        rule.onNodeWithTag("username").assertExists().performTextInput(username)
        rule.awaitIdle()
        rule.onNodeWithTag("password").assertExists().performTextInput("secret")
        rule.awaitIdle()
        rule.onNodeWithTag("login").assertExists().performClick()
    }
}
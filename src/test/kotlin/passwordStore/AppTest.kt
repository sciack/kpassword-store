package passwordStore

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.kodein.di.instance
import passwordStore.navigation.NavController
import passwordStore.services.Services
import passwordStore.services.Services.Companion.NONE
import kotlin.test.BeforeTest
import kotlin.test.Test


class AppTest {
    private val di = DiInjection.testDi
    private val user = testUser()

    @get:Rule
    val rule = createComposeRule()

    @BeforeTest
    fun setUp() {
        val serviceModel by di.instance<Services>()
        serviceModel.user = NONE
        val navController by di.instance<NavController>()
        navController.currentScreen.value = Screen.Login
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

    private suspend fun performLogin(username:String = "m.sciachero") {
        rule.awaitIdle()
        rule.onNodeWithTag("username").assertExists().performTextInput(username)
        rule.awaitIdle()
        rule.onNodeWithTag("password").assertExists().performTextInput("secret")
        rule.awaitIdle()
        rule.onNodeWithTag("login").assertExists().performClick()
    }
}
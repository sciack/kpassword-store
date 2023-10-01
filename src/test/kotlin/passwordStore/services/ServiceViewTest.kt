package passwordStore.services

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Rule
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.testUser
import passwordStore.users.UserVM
import passwordStore.utils.currentDateTime
import passwordStore.withLogin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class ServiceViewTest {
    private val di = DiInjection.testDi
    private val user = testUser()
    private val coroutineScope by di.instance<CoroutineScope>()

    @get:Rule
    val rule = createComposeRule()
    private val serviceModel by di.instance<ServicesSM>()

    @BeforeTest
    fun setUp() = runBlocking {
        val userVM by di.instance<UserVM>()

    }

    @AfterTest
    fun tearDown() {

    }

    @Test
    fun `creation of new service should store the result`() {
        runTest {
            val clock by di.instance<Clock>()
            var service = Service()
            rule.setContent {
                val error = remember {
                    mutableStateOf("")
                }
                withLogin(user) {
                    withDI(di) {
                        editService(error, service, {}) {
                            service = it
                        }
                    }
                }
            }
            rule.awaitIdle()
            val expectedService = Service(
                service = "my service",
                username = "a username",
                password = "a password",
                tags = setOf("tag"),
                note = "someNote",
                userid = user.userid,
                dirty = true,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )
            rule.onNodeWithTag("service").performTextInput(expectedService.service)
            rule.onNodeWithTag("username").performTextInput(expectedService.username)
            rule.onNodeWithTag("password").performTextInput(expectedService.password)
            rule.onNodeWithTag("tags").performTextInput(expectedService.tags.first())
            rule.onNodeWithTag("note").performTextInput(expectedService.note)
            rule.awaitIdle()
            rule.onNodeWithTag("submit").performClick()
            rule.awaitIdle()
            assertThat(service, equalTo(expectedService))
        }
    }

    @Test
    fun `should update the result`() {
        runTest {
            val clock by di.instance<Clock>()
            var service = Service(
                service = "my service",
                username = "a username",
                password = "a password",
                tags = setOf("tag"),
                note = "someNote",
                userid = user.userid,
                dirty = false,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )

            rule.setContent {
                val error = remember {
                    mutableStateOf("")
                }
                withLogin(user) {
                    withDI(di) {
                        editService(error, service, {}) {
                            service = it
                        }
                    }
                }

            }
            rule.awaitIdle()
            val expectedService = service.copy(username = "New username", dirty = true)
            rule.onNodeWithTag("username").performTextReplacement(expectedService.username)
            rule.onNodeWithTag("password").performTextReplacement(expectedService.password)
            rule.onNodeWithTag("tags").performTextReplacement(expectedService.tags.first())
            rule.onNodeWithTag("note").performTextReplacement(expectedService.note)
            rule.awaitIdle()
            rule.onNodeWithTag("submit").performClick()
            rule.awaitIdle()
            assertThat(service, equalTo(expectedService))
        }
    }

    @Test
    fun `should be dirty on tag change`() {
        runTest {
            val clock by di.instance<Clock>()
            var service = Service(
                service = "my service",
                username = "a username",
                password = "a password",
                tags = setOf(),
                note = "someNote",
                userid = user.userid,
                dirty = false,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )
            rule.setContent {
                val error = remember {
                    mutableStateOf("")
                }
                withLogin(user) {
                    withDI(di) {
                        editService(error, service, {}) {
                            service = it
                        }
                    }
                }
            }
            rule.awaitIdle()
            val expectedService = service.copy(tags = setOf("Tag"), dirty = true)
            rule.onNodeWithTag("tags").assertExists().performTextReplacement(expectedService.tags.first())
            rule.onNodeWithTag("note").performTextReplacement(expectedService.note)
            rule.awaitIdle()
            rule.onNodeWithTag("submit").assertExists().assertHasClickAction().performClick()
            rule.awaitIdle()
            rule.waitUntil {
                service == expectedService
            }

        }
    }


    @Test
    fun `should not update update the service name`() {
        runTest {
            val clock by di.instance<Clock>()
            var service = Service(
                service = "my service",
                username = "a username",
                password = "a password",
                tags = setOf("tag"),
                note = "someNote",
                userid = user.userid,
                dirty = false,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )
            val expectedService = service.copy()
            rule.setContent {
                val error = remember {
                    mutableStateOf("")
                }
                withLogin(user) {
                    withDI(di) {
                        editService(error, service, {}) {
                            service = it
                        }
                    }
                }
            }

            rule.awaitIdle()
            rule.onNodeWithTag("service").assertIsEnabled()
                .performTextReplacement("new service")
            rule.awaitIdle()
            assertThat(service, equalTo(expectedService))
        }


    }


}
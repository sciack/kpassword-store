package passwordStore.services

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import org.awaitility.kotlin.await
import org.junit.Rule
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.testUser
import passwordStore.users.UserVM
import passwordStore.utils.currentDateTime
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class ServiceViewTest {
    private val di = DiInjection.testDi
    private val user = testUser()
    private val coroutineScope by di.instance<CoroutineScope>()

    @get:Rule
    val rule = createComposeRule()
    private val serviceModel by di.instance<ServiceVM>()

    @BeforeTest
    fun setUp() = runBlocking {
        val userVM by di.instance<UserVM>()
        userVM.loggedUser.value = user
    }

    @AfterTest
    fun tearDown() {

    }

    @Test
    fun newService() {
        runTest {
            var service = Service()
            val clock by di.instance<Clock>()
            rule.setContent {
                withDI(di) {
                    newService(service, {}) {
                        service = it
                    }
                }
            }
            rule.awaitIdle()
            val expectedService = Service(
                service = "my service",
                username = "a username",
                password = "a password",
                tags = listOf("tag"),
                note = "someNote",
                userid = user.userid,
                dirty = true,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )
            rule.onNodeWithTag("service").performTextInput(expectedService.service)
            rule.onNodeWithTag("username").performTextInput(expectedService.username)
            rule.onNodeWithTag("password").performTextInput(expectedService.password)
            rule.onNodeWithTag("tags").performTextInput(expectedService.tags[0])
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
                tags = listOf("tag"),
                note = "someNote",
                userid = user.userid,
                dirty = false,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )
            rule.setContent {
                withDI(di) {
                    newService(service, {}) {
                        service = it
                    }
                }
            }
            rule.awaitIdle()
            val expectedService = service.copy(username = "New username", dirty = true)
            rule.onNodeWithTag("username").performTextReplacement(expectedService.username)
            rule.onNodeWithTag("password").performTextReplacement(expectedService.password)
            rule.onNodeWithTag("tags").performTextReplacement(expectedService.tags[0])
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
                tags = listOf(),
                note = "someNote",
                userid = user.userid,
                dirty = false,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )
            rule.setContent {
                withDI(di) {
                    newService(service, {}) {
                        service = it
                    }
                }
            }
            rule.awaitIdle()
            val expectedService = service.copy(tags = listOf("Tag"), dirty = true)
            rule.onNodeWithTag("tags").assertExists().performTextReplacement(expectedService.tags[0])
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
                tags = listOf("tag"),
                note = "someNote",
                userid = user.userid,
                dirty = false,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )
            val expectedService = service.copy()
            rule.setContent {
                withDI(di) {
                    newService(service, {}) {
                        service = it
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

    @Test
    fun `should import a csv`() {
        val path = Path.of(this::class.java.getResource("/testCsv.csv").toURI())
        val expectedServices = Service(
            service = "service",
            username = "what",
            password = "#4TRMlRNw",
            note = "test",
            tags = listOf("Mine"),
            updateTime = LocalDateTime.parse("2023-08-01T17:38:16.460784"),
            userid = user.userid,
            score = 1.0
        )
        val gitService = Service(
            service = "Github",
            username = "myUser",
            password = "123456",
            note = "https://github.com",
            tags = listOf("Technology", "Git", "Code"),
            updateTime = LocalDateTime.parse("2023-07-27T10:04:55.972140"),
            userid = user.userid,
            score = 1.0
        )

        runTest {
            serviceModel.readFile(path)
            serviceModel.fetchAll()
            await.atMost(2.seconds.toJavaDuration()).untilAsserted {
                assertThat(serviceModel.services.size, equalTo(2))
                assertThat(serviceModel.services.toList(), hasElement(expectedServices))
                assertThat(serviceModel.services.toList(), hasElement(gitService))
            }

        }
    }
}
package passwordStore.services

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.testUser
import kotlin.test.BeforeTest
import kotlin.test.Test


class ServiceViewTest {
    private val di = DiInjection.testDi
    private val user = testUser()

    @get:Rule
    val rule = createComposeRule()

    @BeforeTest
    fun setUp() = runBlocking {
        val serviceModel by di.instance<Services>()
        serviceModel.user = user
    }

    @Test
    fun newService() {
        runBlocking(Dispatchers.Main) {
            var service = Service()
            val clock by di.instance<Clock>()
            rule.setContent {
                withDI(di) {
                    newService() {
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
                dirty = false,
                score = 0.0,
                updateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
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
        runBlocking(Dispatchers.Main) {
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
                updateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
            )
            rule.setContent {
                withDI(di) {
                    newService(service) {
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
        runBlocking(Dispatchers.Main) {
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
                updateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
            )
            rule.setContent {
                withDI(di) {
                    newService(service) {
                        service = it
                    }
                }
            }
            rule.awaitIdle()
            val expectedService = service.copy(tags= listOf("Tag"), dirty = true)
            rule.onNodeWithTag("tags").performTextReplacement(expectedService.tags[0])
            rule.onNodeWithTag("submit").performClick()
            rule.awaitIdle()
            assertThat(service, equalTo(expectedService))
        }
    }


    @Test()
    fun `should not update update the service name`() {
        runBlocking(Dispatchers.Main) {
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
                updateTime = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
            )
            val expectedService = service.copy()
            rule.setContent {
                withDI(di) {
                    newService(service) {
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
}
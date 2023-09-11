package passwordStore.widget

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import org.awaitility.kotlin.await
import org.junit.Rule
import org.kodein.di.compose.localDI
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.services.ServiceVM
import passwordStore.services.ServicesRepository
import passwordStore.tags.TagRepository
import passwordStore.testService
import passwordStore.testUser
import passwordStore.users.UserVM
import passwordStore.withLogin
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TagViewTest {
    private val di = DiInjection.testDi
    private val user = testUser()
    private val servicesRepository by di.instance<ServicesRepository>()
    private val tagRepository by di.instance<TagRepository>()

    @get:Rule
    val rule = createComposeRule()

    @BeforeTest
    fun setup() {
        val userVM by di.instance<UserVM>()

        runBlocking {
            servicesRepository.search(user).forEach {
                servicesRepository.delete(it.service, it.userid)
            }
        }
    }

    @AfterTest
    fun tearDown() {
        runBlocking {
            servicesRepository.search(user).forEach {
                servicesRepository.delete(it.service, it.userid)
            }
        }
    }

    @Test
    fun `should show the tags`() = runTest {
        val service = testService().copy(tags = listOf("Tags"))
        servicesRepository.store(service)
        rule.setContent {
            withLogin(user) {
                withDI(di) {
                    val serviceModel by localDI().instance<ServiceVM>()
                    val cs = rememberCoroutineScope()
                    cs.launch {
                        serviceModel.fetchAll(user)
                    }
                    tagView()
                }
            }
        }
        rule.awaitIdle()
        rule.onNodeWithText("Tags").assertExists()
    }

    @Test
    fun `should search for the tag`() = runTest {
        var service = testService().copy(tags = listOf("Tags"))
        service = servicesRepository.store(service)
        servicesRepository.store(testService(service = "test2"))
        val serviceModel by di.instance<ServiceVM>()
        serviceModel.fetchAll(user)
        await.atMost(Duration.ofSeconds(1)).until {
            serviceModel.services.size == 2
        }
        rule.setContent {
            withLogin(user) {
                withDI(di) {
                    tagView()
                }
            }
        }
        rule.awaitIdle()

        rule.waitUntilAtLeastOneExists(hasText("Tags"), 3000)

        rule.onNodeWithText("Tags", ignoreCase = true).assertExists().performClick()


        rule.awaitIdle()
        await.atMost(Duration.ofSeconds(5)).untilAsserted {
            assertThat(serviceModel.services.toList(), equalTo(listOf(service)))
        }
    }


    companion object {
        val LOGGER = KotlinLogging.logger { }
    }
}


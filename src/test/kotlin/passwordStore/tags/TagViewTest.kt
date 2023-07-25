package passwordStore.tags

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import org.awaitility.kotlin.await
import org.junit.Rule
import org.kodein.di.compose.localDI
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.*
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TagViewTest {
    private val di = DiInjection.testDi
    private val user = testUser()
    private val servicesRepository by di.instance<ServicesRepository>()
    private val tagRepository by di.instance<TagRepository>()

    @get:Rule
    val rule = createComposeRule()

    @BeforeTest
    fun setup() {
        val serviceModel by di.instance<Services>()
        serviceModel.user = user
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
            withDI(di) {
                val serviceModel by localDI().instance<Services>()
                serviceModel.fetchAll()
                tagView()
            }
        }
        rule.awaitIdle()
        val services = servicesRepository.search(user)
        val tags = tagRepository.tags(user)
        LOGGER.warn { "${services}" }
        LOGGER.warn { "${tags}" }
        rule.onNodeWithText("Tags").assertExists()
    }

    @Test
    fun `should search for the tag`() = runTest {
        var service = testService().copy(tags = listOf("Tags"))
        service = servicesRepository.store(service)
        servicesRepository.store(testService(service = "test2"))
        val serviceModel by di.instance<Services>()
        rule.setContent {
            withDI(di) {
                serviceModel.fetchAll()
                tagView()
            }
        }

        rule.onNodeWithText("Tags", ignoreCase = true).assertExists().performClick()
        rule.awaitIdle()
        await.atMost(Duration.ofSeconds(5)).untilAsserted {
            assertThat(serviceModel.services.value, equalTo(listOf(service)))
        }
    }




    companion object {
        val LOGGER = KotlinLogging.logger { }
    }
}


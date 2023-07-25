package passwordStore.tags

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.kodein.di.instance
import passwordStore.*
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains

class TagRepositoryTest {
    private val di = DiInjection.testDi
    private val tagRepository by di.instance<TagRepository>()
    private val servicesRepository by di.instance<ServicesRepository>()
    private val testUser = testUser()

    @AfterTest
    fun tearDown() {
        runBlocking {
            servicesRepository.search(testUser).forEach {
                servicesRepository.delete(it.service, it.userid)
            }
        }
    }

    @Test
    fun `should retrieve tags`() {
        runTest {
            val service = testService().copy(tags= listOf("Tags"))
            servicesRepository.store(service)
            val tags = tagRepository.tags(testUser)
            assertContains(tags, "Tags")
            assertThat(tags["Tags"], equalTo(1))
        }
    }
}
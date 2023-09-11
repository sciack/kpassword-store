package passwordStore.tags

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.services.ServicesRepository
import passwordStore.testService
import passwordStore.testUser
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
            val service = testService().copy(tags = listOf("Tags"))
            servicesRepository.store(service)
            val tags = tagRepository.tags(testUser)
            assertContains(tags, Tag("Tags", 1))
        }
    }
}
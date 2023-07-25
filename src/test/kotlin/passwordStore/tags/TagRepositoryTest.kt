package passwordStore.tags

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.test.runTest
import org.kodein.di.instance
import passwordStore.*
import kotlin.test.Test
import kotlin.test.assertContains

class TagRepositoryTest {
    private val di = DiInjection.testDi
    private val tagRepository by di.instance<TagRepository>()
    private val serviceRepository by di.instance<ServicesRepository>()
    private val testUser = testUser()

    @Test
    fun `should retrieve tags`() {
        runTest {
            val service = testService().copy(tags= listOf("Tags"))
            serviceRepository.store(service)
            val tags = tagRepository.tags(testUser)
            assertContains(tags, "Tags")
            assertThat(tags["Tags"], equalTo(1))
        }
    }
}
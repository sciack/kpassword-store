package passwordStore.services

import com.github.javafaker.Faker
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import org.awaitility.kotlin.await
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.testUser
import passwordStore.users.UserVM
import java.io.StringWriter
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class CsvDownloadKtTest {
    private val di = DiInjection.testDi
    private val servicesRepository by di.instance<ServicesRepository>()
    private val logger = KotlinLogging.logger {}
    private val user = testUser()
    private val faker = Faker()

    @BeforeTest
    fun setup() {
        val userVM by di.instance<UserVM>()
        userVM.loggedUser.value = user
    }

    @AfterTest
    fun tearDown() {
        runBlocking {
            servicesRepository.search(user).forEach {
                logger.info { "Delete service $it" }
                servicesRepository.delete(it.service, it.userid)
            }
        }
    }

    @Test
    fun `should store a service`(): Unit = runTest {

        val service = Service(
            service = faker.ancient().god(),
            username = faker.ancient().hero(),
            password = faker.internet().password(),
            note = faker.lebowski().quote(),
            dirty = true,
            updateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            userid = user.userid,
            tags = listOf("Some tag"),
            score = 1.0
        )
        val storedService = servicesRepository.store(service)
        val writer = StringWriter()

        writer.performDownload(di)
        await.atMost(Duration.ofSeconds(2)).untilAsserted {
            val result = writer.toString()
            assertThat(result.lines()[0], equalTo("Service,Username,Password,Notes,Tags,Last Update"))
            assertThat(result, containsSubstring(storedService.service))
            assertThat(result, containsSubstring(storedService.username))
            assertThat(result, containsSubstring(storedService.password))
            assertThat(result, containsSubstring(storedService.note))
        }
    }
}
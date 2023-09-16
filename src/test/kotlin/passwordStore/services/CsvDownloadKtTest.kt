package passwordStore.services

import com.github.javafaker.Faker
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toKotlinLocalDateTime
import org.awaitility.kotlin.await
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.LOGGER
import passwordStore.testUser
import passwordStore.users.UserVM
import java.io.StringWriter
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class CsvDownloadKtTest {
    private val di = DiInjection.testDi
    private val servicesRepository by di.instance<ServicesRepository>()
    private val user = testUser()
    private val faker = Faker()

    @BeforeTest
    fun setup() {
        val userVM by di.instance<UserVM>()
    }

    @AfterTest
    fun tearDown() {
        runBlocking {
            servicesRepository.search(user).forEach {
                LOGGER.info { "Delete service $it" }
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
            updateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).toKotlinLocalDateTime(),
            userid = user.userid,
            tags = setOf("Some tag"),
            url = "https://example.com",
            score = 1.0
        )
        val storedService = servicesRepository.store(service)
        val writer = StringWriter()

        writer.performDownload(di, user)
        await.atMost(Duration.ofSeconds(2)).untilAsserted {
            val result = writer.toString()
            assertThat(result.lines()[0], equalTo("Service,Username,Password,Notes,Tags,Url,Last Update"))
            assertThat(result, containsSubstring(storedService.service))
            assertThat(result, containsSubstring(storedService.username))
            assertThat(result, containsSubstring(storedService.password))
            assertThat(result, containsSubstring(storedService.note))
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
            tags = setOf("Mine"),
            updateTime = kotlinx.datetime.LocalDateTime.parse("2023-08-01T17:38:16.460784"),
            userid = user.userid,
            url = "http://example.com",
            score = 1.0
        )
        val gitService = Service(
            service = "Github",
            username = "myUser",
            password = "123456",
            note = "https://github.com",
            tags = setOf("Technology", "Git", "Code"),
            updateTime = kotlinx.datetime.LocalDateTime.parse("2023-07-27T10:04:55.972140"),
            userid = user.userid,
            score = 1.0
        )
        val importSM by di.instance<ImportSM>()
        runTest {
            importSM.readFile(path, user)

            await.atMost(2.seconds.toJavaDuration()).untilAsserted {
                val services = servicesRepository.search(user, "")
                assertThat(services.size, equalTo(2))
                assertThat(services.toList(), hasElement(expectedServices))
                assertThat(services.toList(), hasElement(gitService))
            }

        }
    }
}
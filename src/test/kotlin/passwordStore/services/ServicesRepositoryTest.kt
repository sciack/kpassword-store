package passwordStore.services

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.isEmpty
import com.natpryce.hamkrest.throws
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mu.KotlinLogging
import org.awaitility.kotlin.await
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.testUser
import java.sql.SQLException
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.AfterTest
import kotlin.test.Test

class ServicesRepositoryTest() {
    private val di = DiInjection.testDi
    private val servicesRepository by di.instance<ServicesRepository>()
    private val logger = KotlinLogging.logger {}
    private val user = testUser()

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
            service = "Test service",
            username = "My username",
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            userid = user.userid,
            tags = listOf("Some tag"),
            score = 1.0
        )
        val storedService = servicesRepository.store(service)
        assertThat(service.copy(dirty = false), equalTo(storedService))
    }

    @Test
    fun `when a service is duplicated should throw an exception`(): Unit = runTest {
        val service = Service(
            service = "Test service",
            username = "My username",
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            userid = user.userid,
            tags = listOf("Some tag"),
            score = 1.0
        )
        servicesRepository.store(service)

        val result = runCatching {
            servicesRepository.store(service)
        }
        assertThat (
            { result.getOrThrow() }, throws<SQLException>()
        )
    }

    @Test
    fun `should trace an event on store`(): Unit = runTest {
        val service = Service(
            service = "Test service",
            username = "My username",
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            userid = user.userid,
            tags = listOf("Some tag"),
            score = 1.0
        )
        val result = servicesRepository.store(service)
        await.atMost(Duration.ofSeconds(5)).untilAsserted {
            runBlocking {
                val list = servicesRepository.history("", false, user)
                assertThat(list, isEmpty.not())
                assertThat(list.any { event -> event.service == result }, equalTo(true))
            }
        }

    }

    @Test
    fun `should be able to search for a tag`(): Unit = runTest {
        val service = Service(
            service = "Test service",
            username = "My username",
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            userid = user.userid,
            tags = listOf("tag"),
            score = 1.0
        )
        val storedService = servicesRepository.store(service)
        val result = servicesRepository.search(user, "", "Tag")
        assertThat(result, isEmpty.not())
        assertThat(result[0].service, equalTo("Test service") )
    }

    @Test
    fun `should be able to delete a service`(): Unit = runTest {
        val service = Service(
            service = "Test service",
            username = "My username",
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            userid = user.userid,
            tags = listOf("tag"),
            score = 1.0
        )
        val storedService = servicesRepository.store(service)
        val services = servicesRepository.search(user)
        assertThat(services, hasElement(storedService))
        servicesRepository.delete(serviceName = service.service, userId = user.userid)
        val newServices = servicesRepository.search(user)
        assertThat(newServices, !hasElement(storedService))
    }
}
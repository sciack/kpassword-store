package passwordStore

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import com.natpryce.hamkrest.throws
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.awaitility.kotlin.await
import org.kodein.di.DI
import org.kodein.di.instance
import passwordStore.users.Roles
import passwordStore.users.User
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
            servicesRepository.search(user.userid).forEach {
                logger.info { "Delete service $it" }
                servicesRepository.delete(it.service, it.userid)
            }
        }
    }

    @Test
    fun `should store a service`(): Unit = runBlocking {
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
    fun `when a service is duplicated should throw an exception`(): Unit = runBlocking {
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
    fun `should trace an event on store`(): Unit = runBlocking {
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

}
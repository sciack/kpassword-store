package passwordStore

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kodein.di.DI
import org.kodein.di.instance
import java.sql.SQLException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ExtendWith(DiInjection::class)
class ServicesRepositoryTest(di: DI) {

    private val servicesRepository by di.instance<ServicesRepository>()
    private val userId = "testUser"
    private val logger = KotlinLogging.logger{}

    @AfterEach
    fun tearDown() {
        runBlocking {
            servicesRepository.search(userId).forEach {
                logger.info{"Delete service $it"}
                servicesRepository.delete(it.service, it.userid)
            }
        }
    }

    @Test
    fun `should store a service`(): Unit = runBlocking{
        val service = Service(
            service= "Test service",
            username = "My username",
            password= "a password",
            note= "Some very long notes",
            dirty = true,
            updateTime= LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            userid = userId,
            tags = listOf("Some tag"),
            score = 1.0
        )
        val storedService = servicesRepository.store(service)
        assertThat(service.copy(dirty = false), equalTo(storedService))
    }

    @Test
    fun `when a service is duplicated should throw an exception`(): Unit = runBlocking{
        val service = Service(
            service= "Test service",
            username = "My username",
            password= "a password",
            note= "Some very long notes",
            dirty = true,
            updateTime= LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
            userid = userId,
            tags = listOf("Some tag"),
            score = 1.0
        )
        servicesRepository.store(service)

        val result = runCatching {
            servicesRepository.store(service)
        }
        assertThrows<SQLException> {
            result.getOrThrow()
        }
    }

}
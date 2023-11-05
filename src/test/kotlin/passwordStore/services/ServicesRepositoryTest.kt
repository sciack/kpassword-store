package passwordStore.services

import com.github.javafaker.Faker
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import mu.KotlinLogging
import org.awaitility.kotlin.await
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.nowWithMicro
import passwordStore.services.audit.AuditEventDeque
import passwordStore.testUser
import passwordStore.utils.EventBus
import java.sql.SQLException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class ServicesRepositoryTest() {
    private val di = DiInjection.testDi
    private val servicesRepository by di.instance<ServicesRepository>()
    private val logger = KotlinLogging.logger {}
    private val user = testUser()

    @BeforeTest
    fun setUp() {
        val auditEventDeque by di.instance<AuditEventDeque>()
        auditEventDeque.register()
    }

    @AfterTest
    fun tearDown() {
        runBlocking {
            servicesRepository.search(user).forEach {
                logger.info { "Delete service $it" }
                servicesRepository.delete(it.service, it.userid)
            }
        }
        val eventBus by di.instance<EventBus>()
        eventBus.clear()
    }

    @Test
    fun `should store a service`(): Unit = runTest {
        val service = Service(
            service = "Test service",
            username = "My username",
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.nowWithMicro(),
            userid = user.userid,
            tags = setOf("Some tag"),
            score = 1.0,
            url = "http://example.com"
        )
        val storedService = servicesRepository.store(service)
        assertThat(service.copy(dirty = false), equalTo(storedService))
    }

    @Test
    fun `when a service is duplicated should throw an exception`(): Unit = runTest {
        val faker = Faker()
        val service = Service(
            service = faker.app().name(),
            username = faker.app().author(),
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.nowWithMicro(),
            userid = user.userid,
            tags = setOf("Some tag"),
            score = 1.0,
            url = "http://example.com"
        )
        servicesRepository.store(service)

        val result = runCatching {
            servicesRepository.store(service)
        }
        assertThat(
            { result.getOrThrow() }, throws<SQLException>()
        )
    }

    @Test
    fun `should trace an event on store`(): Unit = runTest {
        val faker = Faker()
        val service = Service(
            service = faker.app().name(),
            username = faker.app().author(),
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.nowWithMicro(),
            userid = user.userid,
            tags = setOf("Some tag"),
            score = 1.0,
            url = "http://example.com"
        )
        val result = servicesRepository.store(service)
        await.atMost(5.seconds.toJavaDuration()).untilAsserted {
            runBlocking {
                val list = servicesRepository.history("", false, user)
                assertThat(list, isEmpty.not())
                assertThat(list.any { event -> event.service == result }, equalTo(true))
            }
        }

    }

    private suspend fun storeService(tags: Set<String> = setOf()): Service {
        val faker = Faker.instance()
        val service = Service(
            service = faker.name().firstName(),
            username = faker.name().username(),
            password = faker.internet().password(),
            note = faker.dune().quote(),
            dirty = true,
            updateTime = LocalDateTime.nowWithMicro(),
            userid = user.userid,
            tags = tags,
            score = 1.0,
            url = "https://${faker.internet().url()}"
        )
        return servicesRepository.store(service)
    }

    @Test
    fun `should be able to search for a tag`(): Unit = runTest {
        val service = storeService(setOf("Tag"))
        val result = servicesRepository.search(user, "", setOf("Tag"))
        assertThat(result, isEmpty.not())
        assertThat(result[0].service, equalTo(service.service))

    }

    @Test
    fun `should be able to search for multiple tags`(): Unit = runTest {
        val service = storeService(setOf("Tag"))
        val service2 = storeService(setOf("Tag", "Another Tag"))
        val result = servicesRepository.search(user, "", setOf("Tag", "Another Tag"))
        assertThat(result.size, equalTo(1))
        assertThat(result, !hasElement(service))
        assertThat(result, hasElement(service2))
    }

    @Test
    fun `should exclude services not matching the tags`(): Unit = runTest {
        val service = storeService(setOf("Tag"))
        val service2 = storeService(setOf("Tag", "Another Tag"))
        val service3 = storeService(setOf("unrelated"))
        val result = servicesRepository.search(user, "", service2.tags)
        assertThat(result.size, equalTo(1))
        assertThat(result, !hasElement(service))
        assertThat(result, hasElement(service2))
        assertThat(result, !hasElement(service3))
    }

    @Test
    fun `should be able to delete a service`(): Unit = runTest {
        val service = Service(
            service = "Test service",
            username = "My username",
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.nowWithMicro(),
            userid = user.userid,
            tags = setOf("tag"),
            score = 1.0,
            url = "http://example.com"
        )
        val storedService = servicesRepository.store(service)
        val services = servicesRepository.search(user)
        assertThat(services, hasElement(storedService))
        servicesRepository.delete(serviceName = service.service, userId = user.userid)
        val newServices = servicesRepository.search(user)
        assertThat(newServices, !hasElement(storedService))
    }


    @Test
    fun `should not validate an invalid url`() {
        val service = Service(
            service = "Test service",
            username = "My username",
            password = "a password",
            note = "Some very long notes",
            dirty = true,
            updateTime = LocalDateTime.nowWithMicro(),
            userid = user.userid,
            tags = setOf("tag"),
            score = 1.0,
            url = "abc"
        )
        service.validate()
        assertThat(service.validate().isFailure, equalTo(true))
        assertThat(
            service.validate().exceptionOrNull()?.message.orEmpty(),
            containsSubstring("Invalid url")
        )
    }
}
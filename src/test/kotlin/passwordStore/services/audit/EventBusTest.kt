package passwordStore.services.audit

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await

import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.LOGGER
import passwordStore.services.Service
import passwordStore.utils.EventBus
import passwordStore.utils.EventListener
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class EventBusTest {
    private val di = DiInjection.testDi

    @BeforeTest
    fun setUp() {

    }

    @AfterTest
    fun tearDown() {
        val eventBus by di.instance<EventBus>()
        eventBus.clear()
    }

    @Test
    fun `should receive an event from bus`() = runTest {
        val eventBus by di.instance<EventBus>()
        lateinit var message: AuditMessage
        eventBus.subscribe(object : EventListener<AuditMessage> {
            override suspend fun onEvent(event: AuditMessage) {
                message = event
            }
        })
        LOGGER.warn{ "listeners: ${eventBus.listeners}"}
        val sentMessage = AuditMessage(
            event = Event(
                service = Service(),
                action = Action.update
            )
        )
        eventBus.send(sentMessage)
        await.atMost(10.seconds.toJavaDuration()).untilAsserted {
            assertThat(message, equalTo(sentMessage))
        }
    }

    @Test
    fun `should not receive an event from bus`() = runTest {
        val eventBus by di.instance<EventBus>()
        var message: AuditMessage? = null
        eventBus.subscribe(object : EventListener<AuditMessage> {
            override suspend fun onEvent(event: AuditMessage) {
                message = event
            }

        })
        val sentMessage = "Test"
        eventBus.send(sentMessage)

        await.during(200.milliseconds.toJavaDuration()).untilAsserted {
            assertThat(message, absent())
        }
    }
}
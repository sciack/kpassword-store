package passwordStore.services.audit

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await

import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.services.Service
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class EventBusTest {
    private val di = DiInjection.testDi
    private lateinit var job: Job

    @BeforeTest
    fun setUp() {

    }

    @AfterTest
    fun tearDown() {
        if (::job.isInitialized) {
            job.cancel()
        }
    }

    @Test
    fun `should receive an event from bus`() = runTest {
        val eventBus by di.instance<EventBus>()
        lateinit var message: AuditMessage
        job = eventBus.subscribe(object : EventListener<AuditMessage> {
            override suspend fun onEvent(event: AuditMessage) {
                message = event
            }
        })
        val sentMessage = AuditMessage(
            event = Event(
                service = Service(),
                action = Action.update
            )
        )
        eventBus.send(sentMessage)
        await.atMost(2.seconds.toJavaDuration()).untilAsserted {
            assertThat(message, equalTo(sentMessage))
        }

    }
}
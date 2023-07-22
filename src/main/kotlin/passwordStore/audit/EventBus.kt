package passwordStore.audit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class EventBus(private val coroutineScope: CoroutineScope) {

    private val channel = Channel<AuditMessage>(capacity = 100)

    private val listeners = CopyOnWriteArrayList<EventListener<AuditMessage>>()

    suspend fun send(message: AuditMessage) {
        channel.send(message)
    }

    fun subscribe(listener: EventListener<AuditMessage>) {
        listeners.add(listener)
    }

    init {
        coroutineScope.launch {
            channel.consumeAsFlow().collect { message ->
                listeners.map { listener ->
                    async {
                        listener.onEvent(message)
                    }
                }.awaitAll()
            }
        }
    }
}

interface EventListener<AuditMessage> {
    suspend fun onEvent(event: AuditMessage)
}
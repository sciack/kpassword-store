package passwordStore.services.audit

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import java.util.concurrent.CopyOnWriteArrayList

class EventBus(coroutineScope: CoroutineScope) {

    private val channel = Channel<AuditMessage>(capacity = 100)

    private val listeners = CopyOnWriteArrayList<EventListener<AuditMessage>>()

    suspend fun send(message: AuditMessage) {
        channel.send(message)
    }

    fun subscribe(listener: EventListener<AuditMessage>) {
        listeners.add(listener)
    }

    init {
        coroutineScope.launch(Dispatchers.IO) {
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
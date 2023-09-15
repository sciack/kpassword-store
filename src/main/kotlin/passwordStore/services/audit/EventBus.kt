package passwordStore.services.audit

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class EventBus(private val coroutineScope: CoroutineScope) {

    private val channel = MutableSharedFlow<AuditMessage>()

    suspend fun send(message: AuditMessage) {
        channel.emit(message)
    }

    fun subscribe(listener: EventListener<AuditMessage>): Job {
        return coroutineScope.launch {
            channel.collect { message ->
                listener.onEvent(message)

            }
        }
    }
}

interface EventListener<T> {
    suspend fun onEvent(event: T)
}
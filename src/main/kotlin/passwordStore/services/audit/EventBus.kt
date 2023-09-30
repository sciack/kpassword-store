package passwordStore.services.audit

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class EventBus(private val coroutineScope: CoroutineScope) {

    private val channel = MutableSharedFlow<Any>()

    suspend fun send(message: Any) {
        channel.emit(message)
    }

    fun <T> subscribe(listener: EventListener<T>): Job {
        return coroutineScope.launch {
            channel.collect { message ->
                if( listener.accept(message)) {
                    listener.onEvent(message as T)
                }
            }
        }
    }
}

interface EventListener<T> {
    suspend fun onEvent(event: T)

    fun accept(event: Any): Boolean
}
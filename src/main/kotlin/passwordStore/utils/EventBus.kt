package passwordStore.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import passwordStore.LOGGER
import kotlin.reflect.KClass

class EventBus(private val coroutineScope: CoroutineScope) {
    private val listeners: MutableMap<KClass<out Any>, MutableList<EventListener<Any>>> = HashMap()


    private val channel = MutableSharedFlow<Any>()

    suspend fun send(message: Any) {
        channel.emit(message)
    }


    init {
        coroutineScope.launch {
            channel.collect { event ->
                listeners[event::class]?.forEach { it.onEvent(event) }
            }
        }
    }

    fun <T : Any> subscribe(eventClass: KClass<out T>, listener: EventListener<T>) {
        val eventListeners = listeners.getOrPut(eventClass) { ArrayList() }
        eventListeners.add(listener as EventListener<Any>)
        LOGGER.info { "Registered subscribers: $listeners" }
    }

    inline fun <reified T : Any> subscribe(listener: EventListener<T>) {
        subscribe(T::class, listener)
    }

    fun <T : Any> unsubscribe(eventClass: KClass<out T>, listener: EventListener<T>) {
        LOGGER.info { "Unsubscribe listener; $listener" }
        listeners.getOrPut(eventClass) { ArrayList() }.remove(listener as EventListener<Any>)
    }

    inline fun <reified T : Any> unsubscribe(listener: EventListener<T>) {
        unsubscribe(T::class, listener)
    }


}

interface EventListener<T> {
    suspend fun onEvent(event: T)

}
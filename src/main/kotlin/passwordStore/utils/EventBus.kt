package passwordStore.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import passwordStore.LOGGER
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

class EventBus(private val coroutineScope: CoroutineScope) {
    val listeners: MutableMap<KClass<out Any>, MutableList<EventListener<Any>>> = HashMap()


    private val channel = MutableSharedFlow<Any>(extraBufferCapacity = 100)

    suspend fun send(message: Any) {
        channel.emit(message)
    }


    init {
        coroutineScope.launch(Dispatchers.IO) {
            channel.collect { event ->
                LOGGER.debug {"Receiving event $event"}
                listeners[event::class]?.forEach {
                    LOGGER.debug {"Processing $event by $it"}
                    it.onEvent(event)
                }
            }
        }
    }

    fun <T : Any> subscribe(eventClass: KClass<out T>, listener: EventListener<T>) {
        val eventListeners = listeners.getOrPut(eventClass) { CopyOnWriteArrayList() }
        eventListeners.add(listener as EventListener<Any>)
        LOGGER.debug { "Registered subscribers: $listeners" }
    }

    inline fun <reified T : Any> subscribe(listener: EventListener<T>) {
        subscribe(T::class, listener)
    }

    fun <T : Any> unsubscribe(eventClass: KClass<out T>, listener: EventListener<T>) {
        LOGGER.debug { "Unsubscribe listener; $listener" }
        listeners.getOrPut(eventClass) { CopyOnWriteArrayList() }.remove(listener as EventListener<Any>)
    }

    inline fun <reified T : Any> unsubscribe(listener: EventListener<T>) {
        unsubscribe(T::class, listener)
    }

    fun clear() {
        listeners.clear()
    }

}

interface EventListener<T> {
    suspend fun onEvent(event: T)

}
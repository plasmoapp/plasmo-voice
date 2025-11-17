package su.plo.voice.event

import com.google.common.collect.Maps
import su.plo.voice.BaseVoice
import su.plo.voice.api.event.Event
import su.plo.voice.api.event.EventBus
import su.plo.voice.api.event.EventCancellable
import su.plo.voice.api.event.EventHandler
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

private val logger = BaseVoice.createLogger(VoiceEventBus::class.java.simpleName)

class VoiceEventBus(
    private val asyncExecutor: ExecutorService,
) : EventBus {
    private val handlersByListeners: MutableMap<Any, MutableList<EventHandler<*>>> = HashMap()
    private val listenersByAddons: MutableMap<Any, MutableList<Any>> = HashMap()
    private val handlersByAddons: MutableMap<Any, MutableList<EventHandler<*>>> = HashMap()

    private val handlers: MutableMap<Class<*>, HandlerList> = Maps.newConcurrentMap()

    override fun <E : Event> fire(event: E): Boolean {
        val handlerList = this.handlers[event.javaClass] ?: return true

        for (eventHandler in handlerList.handlers) {
            eventHandler.execute(event)
        }

        if (event is EventCancellable) {
            return !event.isCancelled()
        }

        return true
    }

    override fun <E : Event> fireAsync(event: E): CompletableFuture<E> {
        val future = CompletableFuture<E>()

        asyncExecutor.execute {
            fire(event)
            future.complete(event)
        }

        return future
    }

    @Synchronized
    override fun register(addon: Any, listener: Any) {
        val methods = (listener.javaClass.methods + listener.javaClass.declaredMethods).distinct()
        methods
            .filter { !it.isBridge && !it.isSynthetic }
            .filter { it.parameterCount == 1 }
            .filter { it.isAnnotationPresent(EventSubscribe::class.java) }
            .map { method ->
                val annotation = method.getAnnotation(EventSubscribe::class.java)

                method to annotation
            }
            .forEach { (method, annotation) ->
                val eventClass = method.parameterTypes[0]
                    .takeIf { Event::class.java.isAssignableFrom(it) }
                    ?.asSubclass(Event::class.java)
                    ?: return@forEach

                method.isAccessible = true

                val handler = EventHandler<Event> { event ->
                    if (annotation.ignoreCancelled &&
                        event is EventCancellable &&
                        event.isCancelled()
                    ) return@EventHandler

                    try {
                        method.invoke(listener, event)
                    } catch (e: Throwable) {
                        logger.warn("Failed to fire an event", e)
                    }
                }

                addHandler(eventClass, annotation.priority, handler)

                handlersByListeners.getOrPut(listener) { mutableListOf() }.add(handler)
            }

        if (handlersByListeners.containsKey(listener)) {
            listenersByAddons.getOrPut(addon) { mutableListOf() }.add(listener)
        }
    }

    @Synchronized
    override fun <E : Event> register(
        addon: Any,
        eventClass: Class<E>,
        priority: EventPriority,
        handler: EventHandler<E>,
    ) {
        addHandler(eventClass, priority, handler.eraseEventType())

        handlersByAddons.getOrPut(addon) { mutableListOf() }.add(handler)
    }

    @Synchronized
    override fun unregister(addon: Any) {
        val handlersToRemove = mutableListOf<EventHandler<*>>()

        listenersByAddons.remove(addon)
            ?.mapNotNull { handlersByListeners.remove(it) }
            ?.forEach { handlersToRemove.addAll(it) }

        handlersByAddons.remove(addon)
            ?.let { handlersToRemove.addAll(it) }

        if (handlersToRemove.isNotEmpty()) removeHandlers(handlersToRemove)
    }

    @Synchronized
    override fun unregister(addon: Any, listener: Any) {
        val addonListeners = listenersByAddons[addon]
        if (addonListeners != null) {
            addonListeners.remove(listener)
            if (addonListeners.isEmpty()) listenersByAddons.remove(addon)
        }

        handlersByListeners.remove(listener)
            ?.let { removeHandlers(it) }
    }

    @Synchronized
    override fun unregister(addon: Any, handler: EventHandler<*>) {
        val addonHandlers = handlersByAddons[addon] ?: return
        addonHandlers.remove(handler)
        if (addonHandlers.isEmpty()) handlersByAddons.remove(addon)

        removeHandlers(listOf(handler))
    }

    override fun hasListener(eventClass: Class<*>): Boolean =
        handlers.containsKey(eventClass)

    @Suppress("UNCHECKED_CAST")
    private fun <E : Event> EventHandler<E>.eraseEventType(): EventHandler<Event> =
        this as EventHandler<Event>

    private fun addHandler(
        eventClass: Class<out Event>,
        priority: EventPriority,
        handler: EventHandler<Event>,
    ) {
        handlers[eventClass] = handlers[eventClass]?.add(priority, handler)
            ?: HandlerList.of(priority, handler)
    }

    private fun removeHandlers(handlersToRemove: Collection<EventHandler<*>>) {
        if (handlersToRemove.isEmpty()) return

        val toRemove = handlersToRemove.toSet()

        for (eventClass in handlers.keys) {
            handlers.computeIfPresent(eventClass) { _, handlerList ->
                handlerList.remove(toRemove).takeIf { it.handlers.isNotEmpty() }
            }
        }
    }

    private class HandlerList private constructor(
        val priorities: Array<EventPriority>,
        val handlers: Array<EventHandler<Event>>,
    ) {
        fun add(priority: EventPriority, handler: EventHandler<Event>): HandlerList {
            val index = priorities.indexOfFirst { it > priority }
                .takeIf { it >= 0 }
                ?: priorities.size

            return HandlerList(
                (priorities.take(index) + priority + priorities.drop(index)).toTypedArray(),
                (handlers.take(index) + handler + handlers.drop(index)).toTypedArray(),
            )
        }

        fun remove(handlersToRemove: Set<EventHandler<*>>): HandlerList {
            val kept = handlers.indices.filter { handlers[it] !in handlersToRemove }
            if (kept.size == handlers.size) return this

            return HandlerList(
                kept.map { priorities[it] }.toTypedArray(),
                kept.map { handlers[it] }.toTypedArray(),
            )
        }

        companion object {
            fun of(priority: EventPriority, handler: EventHandler<Event>): HandlerList =
                HandlerList(arrayOf(priority), arrayOf(handler))
        }
    }
}

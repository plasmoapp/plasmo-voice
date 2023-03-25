package su.plo.lib.api.event

import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Function

/**
 * This events can be fired before Plasmo Voice addons initialization, so use with caution
 */
abstract class MinecraftGlobalEvent<T> (
    private val invokerSupplier: Function<Collection<T>, T>
) {
    private val listeners: MutableSet<T> = CopyOnWriteArraySet()

    var invoker = invokerSupplier.apply(listeners)

    fun registerListener(listener: T) {
        if (listeners.add(listener)) invokerSupplier.apply(listeners)
    }

    fun unregisterListener(listener: T) {
        if (listeners.remove(listener)) invokerSupplier.apply(listeners)
    }
}

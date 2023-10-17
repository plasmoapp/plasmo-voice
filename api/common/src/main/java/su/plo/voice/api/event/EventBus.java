package su.plo.voice.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Manages events and event handlers.
 */
public interface EventBus {

    /**
     * Fires the specified event synchronously.
     *
     * @param event The event to fire.
     * @param <E>   The type of the event.
     * @return {@code false} if the event was cancelled or {@code true} if it was not.
     */
    <E extends Event> boolean fire(@NotNull E event);

    /**
     * Fires the specified event asynchronously.
     *
     * @param event The event to fire asynchronously.
     * @param <E>   The type of the event.
     * @return A {@link CompletableFuture} representing the posted event.
     */
    <E extends Event> CompletableFuture<E> callAsync(@NotNull E event);

    /**
     * Registers all methods annotated with {@link EventSubscribe} as event handlers in the specified listener
     * with the associated addon.
     *
     * @param addon    The addon to associate with the event handlers.
     * @param listener The object with event handlers to register.
     */
    void register(@NotNull Object addon, @NotNull Object listener);

    /**
     * Registers an event handler with the specified priority.
     *
     * @param addon      The addon to associate with the event handler.
     * @param eventClass The class of the event to handle.
     * @param priority   The priority of the event handler in execution.
     * @param handler    The event handler to register.
     * @param <E>        The type of the event.
     */
    <E extends Event> void register(@NotNull Object addon, Class<E> eventClass, EventPriority priority, @NotNull EventHandler<E> handler);

    /**
     * Unregisters all event handlers associated with the specified addon.
     *
     * @param addon The addon for which to unregister event handlers.
     */
    void unregister(@NotNull Object addon);

    /**
     * Unregisters all methods annotated with {@link EventSubscribe} as event handlers in the specified listener
     * with the associated addon.
     *
     * @param addon    The addon associated with the event listener.
     * @param listener The object with event handlers to unregister.
     */
    void unregister(@NotNull Object addon, @NotNull Object listener);

    /**
     * Unregisters the specified event handler associated with the addon.
     *
     * @param addon   The addon associated with the event handler.
     * @param handler The event handler to unregister.
     */
    void unregister(@NotNull Object addon, @NotNull EventHandler<?> handler);
}

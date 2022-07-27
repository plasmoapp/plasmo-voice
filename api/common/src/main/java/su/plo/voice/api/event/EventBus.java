package su.plo.voice.api.event;

import org.jetbrains.annotations.NotNull;

public interface EventBus {
    /**
     * Calls the event
     *
     * @param event the event to call
     */
    <E extends Event> void call(@NotNull E event);

    /**
     * Calls the event asynchronously
     *
     * @param event the event to call
     */
    <E extends Event> void callAsync(@NotNull E event);

    /**
     * Registers all events in listener class
     *
     * @param addon    the addon to associate with event listener
     * @param listener the event listener to register
     */
    void register(@NotNull Object addon, @NotNull Object listener);

    /**
     * Registers an event handler with priority
     *
     * @param addon      the addon to associate with event handler
     * @param eventClass the event class
     * @param priority   the event priority in execution
     * @param handler    the event handler to register
     */
    <E extends Event> void register(@NotNull Object addon, Class<E> eventClass, EventPriority priority, @NotNull EventHandler<E> handler);

    /**
     * Unregisters all listeners of the addon
     */
    void unregister(@NotNull Object addon);

    /**
     * Unregisters the listener of the addon
     */
    void unregister(@NotNull Object addon, @NotNull Object listener);

    /**
     * Unregisters the event handler of the addon
     */
    void unregister(@NotNull Object addon, @NotNull EventHandler<?> handler);
}

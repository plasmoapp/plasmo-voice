package su.plo.voice.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.addon.VoiceAddonManager;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.event.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class VoiceEventBus implements EventBus {

    private static final Logger LOGGER = LogManager.getLogger(VoiceAddonManager.class);

    // listener -> event handlers
    private final Map<Object, List<EventHandler<?>>> registeredListeners = Maps.newConcurrentMap();

    // addon -> listeners
    private final Map<Object, List<Object>> registeredAddonListeners = Maps.newConcurrentMap();

    // addon -> event handlers
    private final Map<Object, List<EventHandler<?>>> registeredAddonHandlers = Maps.newConcurrentMap();

    // class -> handlers
    private final Map<Class<? extends Event>, EnumMap<EventPriority, List<EventHandler<?>>>> handlers = Maps.newConcurrentMap();

    private final Executor asyncExecutor = Executors.newSingleThreadExecutor();

    @Override
    public <E extends Event> void call(@NotNull E event) {
        if (!this.handlers.containsKey(event.getClass())) return;

        for (Map.Entry<EventPriority, List<EventHandler<?>>> entry :
                this.handlers.get(event.getClass()).entrySet()) {
            List<EventHandler<?>> listeners = entry.getValue();

            for (EventHandler listener : listeners) {
                listener.execute(event);
            }
        }
    }

    @Override
    public <E extends Event> void callAsync(@NotNull E event) {
        if (event instanceof EventCancellable) throw new IllegalArgumentException("Cancellable event cannot be run async");
        asyncExecutor.execute(() -> call(event));
    }

    @Override
    public void register(@NotNull Object addon, @NotNull Object listener) {
        checkIfAddon(addon);

        Method[] publicMethods = listener.getClass().getMethods();
        Method[] privateMethods = listener.getClass().getDeclaredMethods();

        Set<Method> methods = new HashSet<>(publicMethods.length + privateMethods.length);
        Collections.addAll(methods, publicMethods);
        Collections.addAll(methods, privateMethods);

        for (Method method : methods) {
            EventSubscribe entry = method.getAnnotation(EventSubscribe.class);
            if (entry == null || method.isBridge() || method.isSynthetic())
                continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length < 1) continue;

            Class<?> clazz = params[0];
            if (params.length != 1 || !Event.class.isAssignableFrom(clazz)) continue;

            final Class<? extends Event> eventClass = clazz.asSubclass(Event.class);

            method.setAccessible(true);
            EventHandler<?> handler = (event) -> {
                try {
                    method.invoke(listener, event);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to call an event: {}", e.getMessage());
                }
            };

            EnumMap<EventPriority, List<EventHandler<?>>> listeners = this.handlers.get(eventClass);
            if (listeners == null) {
                listeners = new EnumMap<>(EventPriority.class);
                handlers.put(eventClass, listeners);
            }

            listeners.compute(
                    entry.priority(),
                    (p, eventHandlers) -> {
                        if (eventHandlers == null) eventHandlers = new CopyOnWriteArrayList<>();
                        eventHandlers.add(handler);
                        return eventHandlers;
                    }
            );

            this.registeredListeners.compute(
                    listener,
                    (l, listenerHandlers) -> {
                        if (listenerHandlers == null) listenerHandlers = new CopyOnWriteArrayList<>();
                        listenerHandlers.add(handler);
                        return listenerHandlers;
                    }
            );
        }

        if (registeredListeners.containsKey(listener)) {
            registeredAddonListeners.compute(
                    addon,
                    (a, addonListeners) -> {
                        if (addonListeners == null) addonListeners = new CopyOnWriteArrayList<>();
                        addonListeners.add(listener);
                        return addonListeners;
                    }
            );
        }
    }

    @Override
    public <E extends Event> void register(@NotNull Object addon, Class<E> eventClass, EventPriority priority, @NotNull EventHandler<E> handler) {
        checkIfAddon(addon);

        EnumMap<EventPriority, List<EventHandler<?>>> listeners = this.handlers.get(eventClass);
        if (listeners == null) {
            listeners = new EnumMap<>(EventPriority.class);
            handlers.put(eventClass, listeners);
        }

        listeners.compute(
                priority,
                (p, eventHandlers) -> {
                    if (eventHandlers == null) eventHandlers = new CopyOnWriteArrayList<>();
                    eventHandlers.add(handler);
                    return eventHandlers;
                }
        );

        registeredAddonHandlers.compute(
                addon,
                (a, addonHandlers) -> {
                    if (addonHandlers == null) addonHandlers = new CopyOnWriteArrayList<>();
                    addonHandlers.add(handler);
                    return addonHandlers;
                }
        );
    }

    @Override
    public void unregister(@NotNull Object addon) {
        checkIfAddon(addon);

        List<EventHandler<?>> handlersToRemove = new ArrayList<>();

        List<Object> addonListeners = registeredAddonListeners.remove(addon);
        if (addonListeners != null) {
            for (Object listener : addonListeners) {
                List<EventHandler<?>> handlers = registeredListeners.remove(listener);
                if (handlers != null) handlersToRemove.addAll(handlers);
            }
        }

        List<EventHandler<?>> addonHandlers = registeredAddonHandlers.remove(addon);
        if (addonHandlers != null) handlersToRemove.addAll(addonHandlers);

        if (handlersToRemove.size() > 0) removeHandlers(handlersToRemove);
    }

    @Override
    public void unregister(@NotNull Object addon, @NotNull Object listener) {
        checkIfAddon(addon);

        List<Object> addonListeners = registeredAddonListeners.get(addon);
        addonListeners.remove(listener);
        if (addonListeners.size() == 0) registeredAddonListeners.remove(addon);

        List<EventHandler<?>> listenerHandlers = registeredListeners.remove(listener);

        if (listenerHandlers != null && listenerHandlers.size() > 0) removeHandlers(listenerHandlers);
    }

    @Override
    public void unregister(@NotNull Object addon, @NotNull EventHandler<?> handler) {
        checkIfAddon(addon);

        List<EventHandler<?>> addonHandlers = registeredAddonHandlers.get(addon);
        addonHandlers.remove(handler);
        if (addonHandlers.size() == 0) registeredAddonHandlers.remove(addon);

        removeHandlers(ImmutableList.of(handler));
    }

    private void checkIfAddon(@NotNull Object addon) {
        if (!addon.getClass().isAnnotationPresent(Addon.class) && !(addon instanceof PlasmoVoice)) {
            throw new IllegalArgumentException("object is not annotated with @Addon");
        }
    }

    private void removeHandlers(List<EventHandler<?>> handlersToRemove) {
        List<Class<? extends Event>> eventsToRemove = new ArrayList<>();

        handlers.forEach((eventClass, listeners) -> {
            List<EventPriority> listenersToRemove = new ArrayList<>();

            listeners.forEach((priority, handlers) -> {
                handlers.removeAll(handlersToRemove);
                if (handlers.size() == 0) {
                    listenersToRemove.add(priority);
                }
            });

            listenersToRemove.forEach(listeners::remove);
            if (listeners.size() == 0) {
                eventsToRemove.add(eventClass);
            }
        });

        eventsToRemove.forEach(handlers::remove);
    }
}

package su.plo.voice.api.event;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = METHOD)
@Retention(value = RUNTIME)
public @interface EventSubscribe {

    /**
     * Defines the priority in execution of the event handler method.
     *
     * @return The priority in execution for the event handler method.
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Specifies whether the event handler should ignore events that have been cancelled.
     * If set to true, the event handler will not be called for cancelled events.
     * If set to false, the event handler will be called regardless of the cancellation status of the event.
     *
     * @return true if the event handler should ignore cancelled events, false otherwise.
     */
    boolean ignoreCancelled() default true;
}

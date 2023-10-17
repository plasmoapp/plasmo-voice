package su.plo.voice.api.event;

/**
 * An interface representing an event that can be cancelled.
 * Events implementing this interface can have their execution cancelled, preventing further processing.
 */
public interface EventCancellable {

    /**
     * Checks the cancellation state of this event.
     *
     * @return {@code true} if this event is cancelled, {@code false} otherwise.
     */
    boolean isCancelled();

    /**
     * Sets the cancellation state of this event.
     * A cancelled event will not be executed, preventing further processing.
     *
     * @param cancel {@code true} if you wish to cancel this event, {@code false} otherwise.
     */
    void setCancelled(boolean cancel);
}

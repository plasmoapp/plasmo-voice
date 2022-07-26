package su.plo.voice.api.event;

public interface EventCancellable {
    /**
     * Gets the cancellation state of this event.
     * A cancelled event will not be executed
     *
     * @return true if this event is cancelled
     */
    boolean isCancelled();

    /**
     * Sets the cancellation state of this event.
     * A cancelled event will not be executed
     *
     * @param cancel true if you wish to cancel this event
     */
    void setCancelled(boolean cancel);
}

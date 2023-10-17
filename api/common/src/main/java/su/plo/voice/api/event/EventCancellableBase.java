package su.plo.voice.api.event;

/**
 * Base implementation of {@link EventCancellable}
 */
public abstract class EventCancellableBase implements Event, EventCancellable {

    private boolean cancel;

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}

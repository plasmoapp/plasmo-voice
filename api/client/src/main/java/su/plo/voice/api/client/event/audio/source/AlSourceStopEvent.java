package su.plo.voice.api.client.event.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.source.AlSource;
import su.plo.voice.api.event.EventCancellable;

/**
 * This event is fired when the {@link AlSource#stop()} has been called
 */
public final class AlSourceStopEvent extends AlSourceEvent implements EventCancellable {

    private boolean cancel;

    public AlSourceStopEvent(@NotNull AlSource source) {
        super(source);
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}

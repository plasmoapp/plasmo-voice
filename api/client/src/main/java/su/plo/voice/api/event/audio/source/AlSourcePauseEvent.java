package su.plo.voice.api.event.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AlSource;
import su.plo.voice.api.event.EventCancellable;

/**
 * This event is called when the {@link AlSource#pause()} has been called
 */
public final class AlSourcePauseEvent extends AlSourceEvent implements EventCancellable {

    private boolean cancel;

    public AlSourcePauseEvent(@NotNull AlSource source) {
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

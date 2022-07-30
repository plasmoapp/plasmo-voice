package su.plo.voice.api.event.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AlSource;

/**
 * This event is called once the {@link AlSource} has been closed
 */
public final class AlSourceClosedEvent extends AlSourceEvent {

    public AlSourceClosedEvent(@NotNull AlSource source) {
        super(source);
    }
}

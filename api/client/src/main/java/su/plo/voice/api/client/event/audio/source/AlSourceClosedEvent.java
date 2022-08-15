package su.plo.voice.api.client.event.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.source.AlSource;

/**
 * This event is fired once the {@link AlSource} has been closed
 */
public final class AlSourceClosedEvent extends AlSourceEvent {

    public AlSourceClosedEvent(@NotNull AlSource source) {
        super(source);
    }
}

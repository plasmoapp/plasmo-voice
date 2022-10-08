package su.plo.voice.api.client.event.audio.device.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.source.AlSource;

/**
 * This event is fired once the {@link AlSource#play()} has been stopped
 */
public final class AlStreamSourceStoppedEvent extends AlSourceEvent {

    public AlStreamSourceStoppedEvent(@NotNull AlSource source) {
        super(source);
    }
}

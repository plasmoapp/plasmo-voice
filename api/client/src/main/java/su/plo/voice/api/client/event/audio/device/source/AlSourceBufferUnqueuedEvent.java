package su.plo.voice.api.client.event.audio.device.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.source.AlSource;

/**
 * This event is fired once the buffer has been unqueued from the AL source.
 */
public final class AlSourceBufferUnqueuedEvent extends AlSourceEvent {

    @Getter
    private final int bufferId;

    public AlSourceBufferUnqueuedEvent(@NotNull AlSource source, int bufferId) {
        super(source);
        this.bufferId = bufferId;
    }
}

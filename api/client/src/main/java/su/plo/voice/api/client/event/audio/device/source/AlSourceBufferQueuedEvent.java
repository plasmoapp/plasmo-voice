package su.plo.voice.api.client.event.audio.device.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.source.AlSource;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the byte buffer has been queued to the AL source.
 */
public final class AlSourceBufferQueuedEvent extends AlSourceEvent {

    @Getter
    private final ByteBuffer buffer;
    @Getter
    private final int bufferId;

    public AlSourceBufferQueuedEvent(@NotNull AlSource source, @NotNull ByteBuffer buffer, int bufferId) {
        super(source);
        this.buffer = checkNotNull(buffer, "buffer cannot be null");
        this.bufferId = bufferId;
    }
}

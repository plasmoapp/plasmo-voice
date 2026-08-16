package su.plo.voice.api.client.event.audio.device.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.util.AudioUtil;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the samples have been queued to the AL source.
 */
public final class AlSourceBufferQueuedEvent extends AlSourceEvent {

    @Getter
    private final short[] samples;
    @Getter
    private final int bufferId;

    private volatile ByteBuffer buffer = null;

    /**
     * @deprecated use {@link AlSourceBufferQueuedEvent#AlSourceBufferQueuedEvent(AlSource, short[], int)} instead.
     */
    @Deprecated
    public AlSourceBufferQueuedEvent(@NotNull AlSource source, @NotNull ByteBuffer buffer, int bufferId) {
        super(source);
        this.buffer = checkNotNull(buffer, "buffer cannot be null");
        byte[] bytes = new byte[buffer.remaining()];
        buffer.duplicate().get(bytes);
        this.samples = AudioUtil.bytesToShorts(bytes);
        this.bufferId = bufferId;
    }

    public AlSourceBufferQueuedEvent(@NotNull AlSource source, short[] samples, int bufferId) {
        super(source);
        this.samples = checkNotNull(samples, "samples cannot be null");
        this.bufferId = bufferId;
    }

    /**
     * Gets the queued samples as a byte buffer.
     *
     * @return The queued audio samples.
     * @deprecated use {@link AlSourceBufferQueuedEvent#getSamples()} instead.
     */
    @Deprecated
    public @NotNull ByteBuffer getBuffer() {
        if (buffer == null) {
            buffer = ByteBuffer.wrap(AudioUtil.shortsToBytes(samples));
        }

        return buffer;
    }
}

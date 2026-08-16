package su.plo.voice.api.client.event.audio.device.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.event.EventCancellable;
import su.plo.voice.api.util.AudioUtil;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the {@link AlSource#write(short[], boolean)} has been invoked.
 */
public final class AlSourceWriteEvent extends AlSourceEvent implements EventCancellable {

    private short[] samples;

    private boolean cancel;

    /**
     * @deprecated use {@link AlSourceWriteEvent#AlSourceWriteEvent(AlSource, short[])} instead.
     */
    @Deprecated
    public AlSourceWriteEvent(@NotNull AlSource source, byte[] samples) {
        super(source);
        this.samples = AudioUtil.bytesToShorts(checkNotNull(samples, "samples cannot be null"));
    }

    public AlSourceWriteEvent(@NotNull AlSource source, short[] samples) {
        super(source);
        this.samples = checkNotNull(samples, "samples cannot be null");
    }

    /**
     * Replaces the samples that will be written to the source.
     *
     * @param samples The audio samples, converted to shorts before being stored.
     * @deprecated use {@link AlSourceWriteEvent#setSamplesShorts(short[])} instead.
     */
    @Deprecated
    public void setSamples(byte[] samples) {
        this.samples = AudioUtil.bytesToShorts(samples);
    }

    /**
     * Gets a copy of the samples that will be written to the source.
     *
     * @return A copy of the audio samples.
     * @deprecated use {@link AlSourceWriteEvent#getSamplesShorts()} instead.
     */
    @Deprecated
    public byte[] getSamples() {
        return AudioUtil.shortsToBytes(samples);
    }

    /**
     * Replaces the samples that will be written to the source.
     *
     * @param samples The audio samples.
     */
    public void setSamplesShorts(short[] samples) {
        this.samples = samples;
    }

    /**
     * Gets the samples that will be written to the source.
     *
     * @return The audio samples.
     */
    public short[] getSamplesShorts() {
        return samples;
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

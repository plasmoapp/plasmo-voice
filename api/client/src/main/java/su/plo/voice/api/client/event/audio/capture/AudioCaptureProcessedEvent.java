package su.plo.voice.api.client.event.audio.capture;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.event.EventCancellableBase;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once audio samples are processed in {@link AudioCapture}.
 */
public final class AudioCaptureProcessedEvent extends EventCancellableBase {

    @Getter
    private final AudioCapture capture;
    @Getter
    private final InputDevice device;
    @Getter
    private final short[] rawSamples;
    @Getter
    private final ProcessedSamples processed;

    public AudioCaptureProcessedEvent(
            @NotNull AudioCapture capture,
            @NotNull InputDevice device,
            short[] rawSamples,
            @NotNull AudioCaptureProcessedEvent.ProcessedSamples processed
    ) {
        this.capture = checkNotNull(capture, "capture");
        this.device = checkNotNull(device, "device");
        this.processed = checkNotNull(processed, "processed");
        this.rawSamples = checkNotNull(rawSamples, "rawSamples");
    }

    /**
     * Represents a samples processed by a {@link InputDevice} in {@link AudioCapture}.
     */
    public interface ProcessedSamples {

        /**
         * Gets a processed samples
         *
         * @return The processed samples.
         */
        default short[] getSamples(boolean stereo) {
            if (stereo) {
                return getStereo();
            } else {
                return getMono();
            }
        }

        /**
         * Gets a processed mono samples.
         *
         * @return The processed mono samples.
         */
        short[] getMono();

        /**
         * Gets a processed stereo samples.
         *
         * @return The processed stereo samples.
         */
        short[] getStereo();
    }
}

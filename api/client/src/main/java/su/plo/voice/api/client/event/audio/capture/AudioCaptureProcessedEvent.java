package su.plo.voice.api.client.event.audio.capture;

import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.event.Event;

/**
 * This event is fired once a samples was processed by {@link AudioCapture}
 */
public final class AudioCaptureProcessedEvent implements Event {

    @Getter
    private final AudioCapture capture;
    @Getter
    private final InputDevice device;
    @Getter
    private final short[] samples;
    @Getter
    private final short[] monoSamplesProcessed;

    public AudioCaptureProcessedEvent(@NonNull AudioCapture capture,
                                      @NonNull InputDevice device,
                                      short[] samples,
                                      short[] monoSamplesProcessed) {
        this.capture = capture;
        this.device = device;
        this.samples = samples;
        this.monoSamplesProcessed = monoSamplesProcessed;
    }

    /**
     * @returns {@link #monoSamplesProcessed} if not null, otherwise copies {@link #samples} and process device filters on it
     */
    public short[] getOrProcessSamples() {
        if (monoSamplesProcessed != null) return monoSamplesProcessed;

        short[] samples = new short[this.samples.length];
        System.arraycopy(this.samples, 0, samples, 0, this.samples.length);

        return device.processFilters(samples);
    }
}

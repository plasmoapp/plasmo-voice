package su.plo.voice.api.client.event.audio.capture;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
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
    private final short[] samplesProcessed;

    public AudioCaptureProcessedEvent(@NonNull AudioCapture capture,
                                      @NonNull InputDevice device,
                                      short[] samples,
                                      short[] samplesProcessed) {
        this.capture = capture;
        this.device = device;
        this.samples = samples;
        this.samplesProcessed = samplesProcessed;
    }
}

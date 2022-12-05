package su.plo.voice.api.client.event.audio.capture;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.event.EventCancellableBase;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired on {@link AudioCapture} start
 */
public final class AudioCaptureStartEvent extends EventCancellableBase {

    @Getter
    private final AudioCapture capture;

    public AudioCaptureStartEvent(@NotNull AudioCapture capture) {
        this.capture = checkNotNull(capture, "capture");
    }
}

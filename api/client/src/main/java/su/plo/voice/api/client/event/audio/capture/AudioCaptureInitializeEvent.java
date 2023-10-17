package su.plo.voice.api.client.event.audio.capture;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.event.EventCancellableBase;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when an {@link AudioCapture} is initialized.
 */
public final class AudioCaptureInitializeEvent extends EventCancellableBase {

    @Getter
    private final AudioCapture capture;

    public AudioCaptureInitializeEvent(@NotNull AudioCapture capture) {
        this.capture = checkNotNull(capture, "capture");
    }
}

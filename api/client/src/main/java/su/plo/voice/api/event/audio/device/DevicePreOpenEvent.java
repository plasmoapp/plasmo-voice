package su.plo.voice.api.event.audio.device;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.device.AudioDevice;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.util.Params;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is called before the device will be open
 */
public final class DevicePreOpenEvent extends EventCancellableBase implements Event {

    @Getter
    private final AudioDevice device;

    @Getter
    private final Params params;

    public DevicePreOpenEvent(@NotNull AudioDevice device, Params params) {
        this.device = checkNotNull(device, "device cannot be null");
        this.params = params;
    }
}

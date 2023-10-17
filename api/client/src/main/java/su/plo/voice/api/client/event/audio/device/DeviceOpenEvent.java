package su.plo.voice.api.client.event.audio.device;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the device has been open.
 */
public final class DeviceOpenEvent implements Event {

    @Getter
    private final AudioDevice device;

    public DeviceOpenEvent(@NotNull AudioDevice device) {
        this.device = checkNotNull(device, "device cannot be null");
    }
}

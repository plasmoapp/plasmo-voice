package su.plo.voice.api.client.event.audio.device;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.source.DeviceSource;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is called once the device source has been created
 */
public final class DeviceSourceCreatedEvent implements Event {

    @Getter
    private final AudioDevice device;

    @Getter
    private final DeviceSource source;

    public DeviceSourceCreatedEvent(@NotNull AudioDevice device, @NotNull DeviceSource source) {
        this.device = checkNotNull(device, "device cannot be null");
        this.source = checkNotNull(source, "source cannot be null");
    }
}

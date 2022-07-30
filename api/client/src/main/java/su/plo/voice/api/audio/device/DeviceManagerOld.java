package su.plo.voice.api.audio.device;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This manager can be used to set primary devices
 */
public interface DeviceManagerOld {

    /**
     * Sets the device
     *
     * @param device the device
     */
    void setDevice(@NotNull AudioDevice device);

    /**
     * Returns the device by its type
     *
     * @param type the device type
     *
     * @return the device
     */
    Optional<AudioDevice> getDevice(@NotNull DeviceType type);
}

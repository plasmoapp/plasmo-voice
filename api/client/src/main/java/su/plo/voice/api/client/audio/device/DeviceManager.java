package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.audio.device.source.SourceGroup;

import java.util.Collection;

/**
 * This manager can be used to add or remove devices
 */
public interface DeviceManager {

    /**
     * Adds new device
     *
     * @param device the device
     */
    void add(@NotNull AudioDevice device);

    /**
     * Replaces old device with the new one
     *
     * @param oldDevice old device
     * @param newDevice new device
     */
    void replace(@NotNull AudioDevice oldDevice, @NotNull AudioDevice newDevice);

    /**
     * Removes the device
     *
     * @param device the device
     */
    void remove(@NotNull AudioDevice device);

    /**
     * Removes all devices
     *
     * @param type the device type (if type is empty then all devices will be removed)
     */
    void clear(@Nullable DeviceType type);

    /**
     * Returns immutable collection of devices by type (if type is empty then all devices will be returned)
     **
     * @param type the device type
     */
    Collection<AudioDevice> getDevices(@Nullable DeviceType type);

    /**
     * Creates a new source group
     *
     * todo: doc
     *
     * @return the source group
     */
    SourceGroup createSourceGroup(@Nullable DeviceType type);
}

package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.util.Params;

import javax.sound.sampled.AudioFormat;
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
     *                  if null first device will be replaced
     * @param newDevice new device
     */
    void replace(@Nullable AudioDevice oldDevice, @NotNull AudioDevice newDevice);

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
    <T extends AudioDevice> Collection<T> getDevices(@Nullable DeviceType type);

    /**
     * Creates a new source group
     *
     * todo: doc
     *
     * @return the source group
     */
    SourceGroup createSourceGroup(@Nullable DeviceType type);

    /**
     * Opens a new input device from config device name
     *
     * @param format audio format
     *               if null current ServerInfo voice format will be used
     * @param params device params, may be different depending on DeviceFactory
     *
     * @return the input device
     *
     * @throws Exception if device cannot be open
     */
    InputDevice openInputDevice(@Nullable AudioFormat format, @NotNull Params params) throws Exception;

    /**
     * Opens a new output device from config device name
     *
     * @param format audio format
     *               if null current ServerInfo voice format will be used
     * @param params device params, may be different depending on DeviceFactory
     *
     * @return the output device
     *
     * @throws Exception if device cannot be open
     */
    OutputDevice<AlSource> openOutputDevice(@Nullable AudioFormat format, @NotNull Params params) throws Exception;

    /**
     * @return params for output device from voice config
     */
    @NotNull Params getDefaultOutputParams();
}

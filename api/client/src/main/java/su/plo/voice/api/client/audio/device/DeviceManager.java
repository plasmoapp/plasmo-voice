package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.DeviceSourceParams;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.client.connection.ServerInfo;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;

/**
 * This manager can be used to add or remove devices
 */
public interface DeviceManager {

    /**
     * Adds a new audio device to the manager.
     *
     * @param device The audio device to add.
     */
    void add(@NotNull AudioDevice device);

    /**
     * Replaces an old audio device with a new one.
     *
     * @param oldDevice The old audio device to replace (or null to replace the first device).
     * @param newDevice The new audio device to use as a replacement.
     */
    void replace(@Nullable AudioDevice oldDevice, @NotNull AudioDevice newDevice);

    /**
     * Removes an audio device from the manager.
     *
     * @param device The audio device to remove.
     */
    void remove(@NotNull AudioDevice device);

    /**
     * Removes all audio devices of the specified type (or all devices if the type is null).
     *
     * @param type The type of audio devices to remove (or null to remove all devices).
     */
    void clear(@Nullable DeviceType type);

    /**
     * Retrieves an immutable collection of audio devices of the specified type (or all devices if the type is null).
     *
     * @param type The type of audio devices to retrieve (or null to retrieve all devices).
     * @return A collection of audio devices.
     */
    <T extends AudioDevice> Collection<T> getDevices(@Nullable DeviceType type);

    /**
     * Creates a new source group for managing audio sources.
     *
     * @see SourceGroup
     * @param type The type of devices associated with the source group (or null for a generic source group).
     * @return A source group for managing audio sources.
     */
    @NotNull SourceGroup createSourceGroup(@Nullable DeviceType type);

    /**
     * Opens a new input device with the specified audio format and device parameters.
     *
     * @param format The audio format (or null to use the current {@link ServerInfo} voice format).
     * @return The input device.
     * @throws DeviceException if the input device cannot be opened.
     */
    @NotNull InputDevice openInputDevice(@Nullable AudioFormat format) throws DeviceException;

    /**
     * Opens a new output device with the specified audio format and device parameters.
     *
     * @param format The audio format (or null to use the current ServerInfo voice format).
     * @return The output device.
     * @throws DeviceException if the output device cannot be opened.
     */
    @NotNull OutputDevice<AlSource> openOutputDevice(@Nullable AudioFormat format) throws DeviceException;
}

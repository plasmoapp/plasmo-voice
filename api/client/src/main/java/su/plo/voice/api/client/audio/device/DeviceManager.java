package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.client.connection.ServerInfo;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;

/**
 * Manages audio devices.
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
     * <br>
     * This method doesn't close the old audio device.
     *
     * @param oldDevice The old audio device to replace (or null to replace the first device).
     * @param deviceType The device type to replace.
     * @param replacementSupplier The new audio device to use as a replacement.
     */
    void replace(
            @Nullable AudioDevice oldDevice,
            @NotNull DeviceType deviceType,
            @NotNull DeviceReplacementSupplier replacementSupplier
    ) throws DeviceException;

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
     * Gets an audio devices collection of the specified type (or all devices if the type is null).
     *
     * @param type The type of audio devices to retrieve (or null to retrieve all devices).
     * @return A collection of audio devices.
     */
    <T extends AudioDevice> Collection<T> getDevices(@Nullable DeviceType type);

    /**
     * Creates a new source group for managing audio sources.
     *
     * @see SourceGroup
     * @return A source group for managing audio sources.
     */
    @NotNull SourceGroup createSourceGroup();

    /**
     * Opens a new input device with the specified audio format and device parameters.
     *
     * @param format The audio format (or null to use the current {@link ServerInfo} voice format).
     * @return The input device.
     * @throws DeviceException If there is an issue with opening an input device.
     */
    @NotNull InputDevice openInputDevice(@Nullable AudioFormat format) throws DeviceException;

    /**
     * Opens a new output device with the specified audio format and device parameters.
     *
     * @param format The audio format (or null to use the current {@link ServerInfo} voice format).
     * @return The output device.
     * @throws DeviceException If there is an issue with opening an output device.
     */
    @NotNull OutputDevice<AlSource> openOutputDevice(@Nullable AudioFormat format) throws DeviceException;

    @FunctionalInterface
    interface DeviceReplacementSupplier {

        /**
         * Creates an audio device replacement.
         *
         * @param device The old audio device.
         * @return The new audio device.
         */
        @NotNull AudioDevice createReplacement(@Nullable AudioDevice device) throws DeviceException;
    }
}

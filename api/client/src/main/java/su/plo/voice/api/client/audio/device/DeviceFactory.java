package su.plo.voice.api.client.audio.device;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;

/**
 * This factory is used to open new audio devices.
 */
public interface DeviceFactory {

    /**
     * Opens a new audio device with the specified audio format and optional device name.
     *
     * @param format     The audio format to use for the device.
     * @param deviceName The optional name of the specific audio device to open, or null to use the default device.
     * @return A new audio device instance.
     * @throws DeviceException If the audio device cannot be opened.
     */
    @NotNull AudioDevice openDevice(@NotNull AudioFormat format, @Nullable String deviceName) throws DeviceException;

    /**
     * Retrieves the default audio device name.
     *
     * @return The default audio device name.
     */
    @NotNull String getDefaultDeviceName();

    /**
     * Retrieves a list of all available audio device names.
     *
     * @return A list of available audio device names.
     */
    @NotNull ImmutableList<String> getDeviceNames();

    /**
     * Retrieves the unique factory type of the audio devices produced by this factory.
     *
     * @return The factory's unique type identifier.
     */
    @NotNull String getType();
}

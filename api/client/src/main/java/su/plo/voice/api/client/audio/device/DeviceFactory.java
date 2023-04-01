package su.plo.voice.api.client.audio.device;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.util.Params;

import javax.sound.sampled.AudioFormat;

// todo: doc
public interface DeviceFactory {

    /**
     * Opens a new device
     *
     * @param deviceName the device name
     *
     * @return a new device
     *
     * @throws DeviceException if device cannot be open
     */
    AudioDevice openDevice(@NotNull AudioFormat format, @Nullable String deviceName) throws DeviceException;

    /**
     * Gets the default device name
     *
     * @return the default device name
     */
    String getDefaultDeviceName();

    /**
     * Gets all device names
     *
     * @return device names
     */
    ImmutableList<String> getDeviceNames();

    /**
     * Gets the device's factory type, should be unique
     *
     * @return the device's factory type
     */
    String getType();
}

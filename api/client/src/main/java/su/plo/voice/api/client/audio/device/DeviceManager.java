package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.connection.ServerInfo;

import javax.sound.sampled.AudioFormat;
import java.util.Optional;

/**
 * Manages audio devices.
 */
public interface DeviceManager {

    /**
     * Gets the current output device.
     *
     * @return The output device or null.
     */
    @NotNull Optional<AlContextOutputDevice> getOutputDevice();

    /**
     * Sets an output device.
     * </br>
     * This method also registers and unregisters (when replaced or removed) the device in event bus.
     *
     * @param device The audio output device, or null to remove a current device from the manager.
     */
    void setOutputDevice(@Nullable AlContextOutputDevice device);

    /**
     * Gets the current input device.
     *
     * @return The output device or null.
     */
    @NotNull Optional<InputDevice> getInputDevice();

    /**
     * Sets an input device.
     * </br>
     * This method also registers and unregisters (when replaced or removed) the device in event bus.
     *
     * @param device The audio output device, or null to remove a current device from the manager.
     */
    void setInputDevice(@Nullable InputDevice device);

    /**
     * Closes and removes the output and input devices.
     */
    default void clear() {
        getOutputDevice().ifPresent(OutputDevice::close);
        setOutputDevice(null);
        getInputDevice().ifPresent(InputDevice::close);
        setInputDevice(null);
    }

    /**
     * Opens a new input device with the specified audio format and device parameters.
     * </br>
     * Note: This method doesn't set the input device in the manager. You need to use {@link #setInputDevice(InputDevice)}.
     *
     * @param format The audio format (or null to use the current {@link ServerInfo} voice format).
     * @return The input device.
     * @throws DeviceException If there is an issue with opening an input device.
     */
    @NotNull InputDevice openInputDevice(@Nullable AudioFormat format) throws DeviceException;

    /**
     * Opens a new output device with the specified audio format and device parameters.
     * </br>
     * Note: This method doesn't set the input device in the manager. You need to use {@link #setInputDevice(InputDevice)}.
     *
     * @param format The audio format (or null to use the current {@link ServerInfo} voice format).
     * @return The output device.
     * @throws DeviceException If there is an issue with opening an output device.
     */
    @NotNull AlContextOutputDevice openOutputDevice(@Nullable AudioFormat format) throws DeviceException;
}

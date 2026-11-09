package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.connection.ServerInfo;

import javax.sound.sampled.AudioFormat;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Manages audio devices.
 */
public interface DeviceManager {

    /**
     * Gets the current output device.
     *
     * @return The output device or empty.
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
     * @return The input device or empty.
     */
    @NotNull Optional<InputDevice> getInputDevice();

    /**
     * Gets the last input device error.
     *
     * @return The input device error or empty.
     */
    @NotNull Optional<Throwable> getInputDeviceError();

    /**
     * Sets an input device.
     * </br>
     * This method also registers and unregisters (when replaced or removed) the device in event bus.
     *
     * @param device The audio output device, or null to remove a current device from the manager.
     */
    default void setInputDevice(@Nullable InputDevice device) {
        setInputDevice(device, null);
    }

    /**
     * Sets an input device.
     * </br>
     * This method also registers and unregisters (when replaced or removed) the device in event bus.
     *
     * @param device The audio output device, or null to remove a current device from the manager.
     * @param lastError An error thrown when trying to open a device.
     */
    void setInputDevice(@Nullable InputDevice device, @Nullable Throwable lastError);

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

    /**
     * Closes the current output device and opens a new one in the background,
     * using the configured device name and the current {@link ServerInfo} voice format.
     * </br>
     * Reloads run one at a time, in the order they were requested.
     * The new device is set in the manager before the returned future completes.
     * If the player disconnects while the device is opening, the device is closed and not set.
     *
     * @return A future that completes with the new device, or with null if the player disconnected,
     * or completes exceptionally if a new device couldn't be opened.
     */
    @NotNull CompletableFuture<@Nullable AlContextOutputDevice> reloadOutputDevice();

    /**
     * Closes the current input device and opens a new one in the background,
     * using the configured device name, input backend and the current {@link ServerInfo} voice format.
     * </br>
     * Reloads run one at a time, in the order they were requested.
     * The new device is set in the manager before the returned future completes.
     * If the player disconnects while the device is opening, the device is closed and not set.
     * Does nothing if the input device is disabled in the config.
     *
     * @return A future that completes with the new device, or with null if the player disconnected
     * or the input device is disabled, or completes exceptionally if a new device couldn't be opened.
     */
    @NotNull CompletableFuture<@Nullable InputDevice> reloadInputDevice();
}

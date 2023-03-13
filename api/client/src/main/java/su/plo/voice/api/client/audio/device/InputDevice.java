package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;

// todo: doc
public interface InputDevice extends AudioDevice {

    /**
     * Start the device
     */
    void start();

    /**
     * Stop the device
     */
    void stop();

    /**
     * @return available samples
     */
    int available();

    /**
     * Read samples with the capacity of bufferSize
     *
     * @param bufferSize the frame size
     * @return samples or null if bufferSize is greater than available samples
     */
    short[] read(int bufferSize);

    /**
     * Read samples with the capacity of bufferSize calculated from AudioFormat
     *
     * @return samples or null if bufferSize is greater than available samples
     */
    default short[] read() {
        return read(getBufferSize());
    }

    @Override
    default @NotNull DeviceType getType() {
        return DeviceType.INPUT;
    }
}

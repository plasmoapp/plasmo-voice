package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an audio input device used for capturing audio data.
 */
public interface InputDevice extends AudioDevice {

    /**
     * Starts the audio input device.
     */
    void start();

    /**
     * Stops the audio input device.
     */
    void stop();

    /**
     * Retrieves the number of available audio samples that can be read from the input device.
     *
     * @return The count of available audio samples.
     */
    int available();

    /**
     * Checks if the input device has been started.
     *
     * @return {@code true} if the device is started, {@code false} otherwise.
     */
    boolean isStarted();

    /**
     * Reads audio samples from the input device with the specified buffer size.
     * The buffer size determines the number of audio samples to read.
     *
     * @param frameSize The size of the frame for reading samples.
     * @return An array of audio samples read from the device, or {@code null} if the buffer size exceeds available samples.
     */
    short[] read(int frameSize);

    /**
     * Reads audio samples from the input device using the buffer size calculated from the AudioFormat.
     *
     * @return An array of audio samples read from the device, or {@code null} if the buffer size exceeds available samples.
     */
    default short[] read() {
        return read(getFrameSize());
    }

    @Override
    default @NotNull DeviceType getType() {
        return DeviceType.INPUT;
    }
}

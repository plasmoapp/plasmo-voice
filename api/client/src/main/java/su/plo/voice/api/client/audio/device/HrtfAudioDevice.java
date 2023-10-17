package su.plo.voice.api.client.audio.device;

/**
 * Represents an audio device with support for Head-Related Transfer Function (HRTF).
 */
public interface HrtfAudioDevice {

    /**
     * Checks if HRTF support is available for this audio device.
     *
     * @return {@code true} if HRTF support is available, {@code false} otherwise.
     */
    boolean isHrtfSupported();

    /**
     * Checks if HRTF is currently enabled for this audio device.
     *
     * @return {@code true} if HRTF is enabled, {@code false} otherwise.
     */
    boolean isHrtfEnabled();

    /**
     * Enables HRTF for this audio device.
     */
    void enableHrtf();

    /**
     * Disables HRTF for this audio device.
     */
    void disableHrtf();
}

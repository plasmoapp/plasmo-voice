package su.plo.voice.api.client.audio.device;

/**
 * Represents an audio device with HRTF support
 */
public interface HrtfAudioDevice {

    boolean isHrtfSupported();

    boolean isHrtfEnabled();

    void enableHrtf();

    void disableHrtf();
}

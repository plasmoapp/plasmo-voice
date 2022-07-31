package su.plo.voice.api.client.audio.device;

import java.util.Optional;

// todo: doc
public interface AlAudioDevice extends AudioDevice {

    /**
     * Gets the device's pointer
     */
    Optional<Long> getPointer();

    /**
     * Gets the device's context pointer
     */
    Optional<Long> getContextPointer();

    /**
     * Runs runnable in the device's context
     */
    void runInContext(Runnable runnable);
}

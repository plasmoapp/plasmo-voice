package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;

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
    void runInContext(@NotNull DeviceRunnable runnable);

    interface DeviceRunnable {

        void run() throws DeviceException;
    }
}

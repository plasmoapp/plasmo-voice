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
    default void runInContext(@NotNull DeviceRunnable runnable) {
        runInContext(runnable, true);
    }

    void runInContext(@NotNull DeviceRunnable runnable, boolean blocking);

    interface DeviceRunnable {

        void run() throws DeviceException;
    }
}

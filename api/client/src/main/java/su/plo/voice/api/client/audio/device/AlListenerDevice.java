package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;

// todo: doc
public interface AlListenerDevice {

    /**
     * Gets the AL device listener
     */
    @NotNull Listener getListener();

    interface Listener {

        /**
         * Updates the player's current position
         */
        void update();
    }
}

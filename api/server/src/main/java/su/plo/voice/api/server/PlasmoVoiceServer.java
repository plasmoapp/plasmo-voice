package su.plo.voice.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.server.player.PlayerManager;

/**
 * The Plasmo Client Server API
 */
public interface PlasmoVoiceServer extends PlasmoVoice {

    /**
     * Gets the {@link PlayerManager}
     *
     * This manager can be used to get voice players
     *
     * @return the player manager
     */
    @NotNull PlayerManager getPlayerManager();
}

package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents "proxy" player to an actual server player object
 */
public interface VoicePlayer {

    /**
     * Gets the player's unique id
     */
    @NotNull UUID getUUID();
}

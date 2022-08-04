package su.plo.voice.api.server.player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * This manager can be used to get voice players
 */
public interface PlayerManager {

    /**
     * Gets the {@link VoicePlayer} by uuid
     */
    Optional<VoicePlayer> getPlayer(UUID uniqueId);

    /**
     * Gets the {@link VoicePlayer} by server player
     */
    Optional<VoicePlayer> getPlayer(Object player);

    /**
     * Gets collection of the players
     */
    Collection<VoicePlayer> getPlayers();
}

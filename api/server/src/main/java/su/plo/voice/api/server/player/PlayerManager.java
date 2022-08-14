package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;

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
    Optional<VoicePlayer> getPlayerById(@NotNull UUID playerId);

    /**
     * Gets the {@link VoicePlayer} by server player
     */
    @NotNull VoicePlayer wrap(@NotNull Object player);

    /**
     * Gets collection of the players
     */
    Collection<VoicePlayer> getPlayers();
}

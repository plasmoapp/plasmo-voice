package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * This manager can be used to get voice players
 */
public interface VoicePlayerManager {

    /**
     * Gets the {@link VoicePlayer} by uuid
     *
     * @param playerId player's unique id
     * @return {@link VoicePlayer}
     */
    Optional<VoicePlayer> getPlayerById(@NotNull UUID playerId);

    /**
     * Gets the {@link VoicePlayer} by server player
     *
     * @param instance player's server object
     *                 org.bukkit.entity.Player for Bukkit+
     *                 net.minecraft.server.level.ServerPlayer for Fabric/Forge/OrSomethingModded
     * @return {@link VoicePlayer}
     */
    @NotNull VoicePlayer wrap(@NotNull Object instance);

    /**
     * Gets collection of the players
     *
     * @return {@link Collection<VoicePlayer>}
     */
    Collection<VoicePlayer> getPlayers();

    /**
     * Registers the permission which will be synchronized with the client API
     *
     * @param permission the permission
     * @throws IllegalArgumentException if permission is already registered
     */
    void registerPermission(@NotNull String permission);

    /**
     * Unregisters the synchronized permission
     *
     * @param permission the permission
     */
    void unregisterPermission(@NotNull String permission);

    /**
     * Gets the collection of synchronized permissions
     *
     * @return {@link Collection<String>}
     */
    Collection<String> getSynchronizedPermissions();
}

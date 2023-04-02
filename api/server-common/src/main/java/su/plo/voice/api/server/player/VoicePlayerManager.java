package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftCommonServerLib;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * This manager can be used to get voice players
 */
public interface VoicePlayerManager<P extends VoicePlayer> {

    /***
     * @param playerId player's unique id
     *
     * @return {@link P} by uuid
     */
    default Optional<P> getPlayerById(@NotNull UUID playerId) {
        return getPlayerById(playerId, true);
    }

    /***
     * @param playerId player's unique id
     * @param useServerInstance should get player instance from {@link MinecraftCommonServerLib}
     *                        if VoicePlayer is not found in {@link VoicePlayerManager} map
     *
     * @return {@link P} by uuid
     */
    Optional<P> getPlayerById(@NotNull UUID playerId, boolean useServerInstance);

    /**
     * @param playerName player's name
     *
     * @return {@link P} by name
     */
    default Optional<P> getPlayerByName(@NotNull String playerName) {
        return getPlayerByName(playerName, true);
    }

    /**
     * @param playerName player's name
     * @param useServerInstance should get player instance from {@link MinecraftCommonServerLib}
     *                        if VoicePlayer is not found in {@link VoicePlayerManager} map
     *
     * @return @return {@link P} by name
     */
    Optional<P> getPlayerByName(@NotNull String playerName, boolean useServerInstance);

    /**
     * Gets the {@link P} by server player
     *
     * @param instance player's server object
     *                 org.bukkit.entity.Player for Bukkit+
     *                 net.minecraft.server.level.ServerPlayer for Fabric/Forge/OrSomethingModded
     *
     * @return {@link P}
     */
    @NotNull P wrap(@NotNull Object instance);

    /**
     * Gets collection of the players
     *
     * @return collection of {@link P}
     */
    Collection<P> getPlayers();

    /**
     * Registers the permission which will be synchronized with the client API
     *
     * @param permission the permission
     *
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
     * @return collection of {@link String}
     */
    Collection<String> getSynchronizedPermissions();
}

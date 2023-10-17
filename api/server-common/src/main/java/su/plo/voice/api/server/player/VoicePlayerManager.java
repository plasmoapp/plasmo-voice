package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.McLib;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Base interface for managing voice players.
 */
public interface VoicePlayerManager<P extends VoicePlayer> {

    /**
     * Gets a voice player by their unique identifier.
     *
     * @param playerId         The unique UUID of the player.
     * @return An optional containing the voice player if found, or an empty optional if not found.
     */
    default Optional<P> getPlayerById(@NotNull UUID playerId) {
        return getPlayerById(playerId, true);
    }

    /**
     * Gets a voice player by their unique identifier.
     *
     * @param playerId         The unique identifier of the player.
     * @param useServerInstance If true, the method will use the server instance from {@link McLib}
     *                         if the VoicePlayer is not found in the {@link VoicePlayerManager} map.
     * @return An optional containing the voice player if found, or an empty optional if not found.
     */
    Optional<P> getPlayerById(@NotNull UUID playerId, boolean useServerInstance);

    /**
     * Gets a voice player by their name.
     *
     * @param playerName       The name of the player.
     * @return An optional containing the voice player if found, or an empty optional if not found.
     */
    default Optional<P> getPlayerByName(@NotNull String playerName) {
        return getPlayerByName(playerName, true);
    }

    /**
     * Gets a voice player by their name.
     *
     * @param playerName       The name of the player.
     * @param useServerInstance If true, the method will use the server instance from {@link McLib}
     *                         if the VoicePlayer is not found in {@link VoicePlayerManager} map.
     * @return An optional containing the voice player if found, or an empty optional if not found.
     */
    Optional<P> getPlayerByName(@NotNull String playerName, boolean useServerInstance);

    /**
     * Gets a voice player by their server-specific instance.
     *
     * <p>
     *     The <b>instance</b> parameter represents the server-specific player instance:
     *     <ul>
     *         <li>For Bukkit: [org.bukkit.entity.Player]</li>
     *         <li>For modded servers (Fabric/Forge): [net.minecraft.server.level.ServerPlayer]</li>
     *     </ul>
     * </p>
     *
     * @param instance The server-specific player instance.
     *
     * @return The voice player.
     */
    @NotNull P getPlayerByInstance(@NotNull Object instance);

    /**
     * Gets a collection of all voice players.
     *
     * @return A collection of voice player instances.
     */
    Collection<P> getPlayers();

    /**
     * Registers a permission that will be synchronized with the client API.
     *
     * @param permission The permission to register.
     * @throws IllegalArgumentException if the permission is already registered.
     */
    void registerPermission(@NotNull String permission);

    /**
     * Unregisters a synchronized permission.
     *
     * @param permission The permission to unregister.
     */
    void unregisterPermission(@NotNull String permission);

    /**
     * Gets a collection of synchronized permissions.
     *
     * @return A collection of permission strings.
     */
    Collection<String> getSynchronizedPermissions();
}

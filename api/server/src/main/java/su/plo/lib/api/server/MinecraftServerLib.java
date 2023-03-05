package su.plo.lib.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;
import su.plo.lib.api.server.permission.PermissionsManager;
import su.plo.lib.api.server.world.MinecraftServerWorld;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MinecraftServerLib extends MinecraftCommonServerLib {

    /**
     * Executes the task on main thread
     */
    void executeInMainThread(@NotNull Runnable runnable);

    /**
     * instance {@link Object} can be:
     * <ul>
     * <li>{@code org.bukkit.World} for bukkit</li>
     * <li>{@code net.minecraft.server.level.ServerLevel} for mods (fabric/forge)</li>
     * </ul>
     * @return {@link MinecraftServerWorld} by server's instance
     */
    @NotNull MinecraftServerWorld getWorld(@NotNull Object instance);

    /**
     * @return collection of all worlds
     */
    Collection<MinecraftServerWorld> getWorlds();

    /**
     * instance {@link Object} can be:
     * <ul>
     * <li>{@code org.bukkit.entity.Player} for bukkit</li>
     * <li>{@code net.minecraft.server.level.ServerPlayer} for mods (fabric/forge)</li>
     * </ul>
     * @return {@link MinecraftServerPlayerEntity} by server's instance
     */
    @NotNull MinecraftServerPlayerEntity getPlayerByInstance(@NotNull Object instance);

    /**
     * @return {@link MinecraftServerPlayerEntity} by name if exists
     */
    Optional<MinecraftServerPlayerEntity> getPlayerByName(@NotNull String name);

    /**
     * @return {@link MinecraftServerPlayerEntity} by uuid if exists
     */
    Optional<MinecraftServerPlayerEntity> getPlayerById(@NotNull UUID playerId);

    /**
     * @return collection of all online players
     */
    @NotNull Collection<MinecraftServerPlayerEntity> getPlayers();

    /**
     * @return {@link MinecraftGameProfile} by player's uuid if exists
     */
    Optional<MinecraftGameProfile> getGameProfile(@NotNull UUID playerId);

    /**
     * @return {@link MinecraftGameProfile} by player's name if exists
     */
    Optional<MinecraftGameProfile> getGameProfile(@NotNull String name);

    /**
     * instance {@link Object} can be:
     * <ul>
     * <li>{@code org.bukkit.entity.Player} for bukkit</li>
     * <li>{@code org.bukkit.entity.LivingEntity} for mods (fabric/forge)</li>
     * </ul>
     * @return {@link MinecraftServerEntity} by server's instance
     */
    @NotNull MinecraftServerEntity getEntity(@NotNull Object instance);

    /**
     * @return server's port
     */
    int getPort();

    /**
     * @return minecraft server version
     */
    @NotNull String getVersion();

    @NotNull MinecraftCommandManager<MinecraftCommand> getCommandManager();

    default void onInitialize() {
    }

    default void onShutdown() {

    }
}

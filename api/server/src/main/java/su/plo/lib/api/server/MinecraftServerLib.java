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

public interface MinecraftServerLib {

    default void onInitialize() {
    }

    default void onShutdown() {
    }

    void executeInMainThread(@NotNull Runnable runnable);

    @NotNull MinecraftServerWorld getWorld(@NotNull Object instance);

    Collection<MinecraftServerWorld> getWorlds();

    @NotNull MinecraftServerPlayerEntity getPlayerByInstance(@NotNull Object instance);

    Optional<MinecraftServerPlayerEntity> getPlayerByName(@NotNull String name);

    Optional<MinecraftServerPlayerEntity> getPlayerById(@NotNull UUID playerId);

    Optional<MinecraftGameProfile> getGameProfile(@NotNull UUID playerId);

    Optional<MinecraftGameProfile> getGameProfile(@NotNull String name);

    @NotNull Collection<MinecraftServerPlayerEntity> getPlayers();

    @NotNull MinecraftServerEntity getEntity(@NotNull Object instance);

    @NotNull MinecraftCommandManager<MinecraftCommand> getCommandManager();

    @NotNull PermissionsManager getPermissionsManager();

    int getPort();
}

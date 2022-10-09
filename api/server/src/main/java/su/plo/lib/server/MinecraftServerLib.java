package su.plo.lib.server;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.profile.MinecraftGameProfile;
import su.plo.lib.server.command.MinecraftCommandManager;
import su.plo.lib.server.entity.MinecraftServerEntity;
import su.plo.lib.server.entity.MinecraftServerPlayer;
import su.plo.lib.server.permission.PermissionsManager;
import su.plo.lib.server.world.MinecraftServerWorld;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MinecraftServerLib {

    default void onInitialize() {
    }

    default void onShutdown() {
    }

    @NotNull MinecraftServerWorld getWorld(@NotNull Object instance);

    @NotNull MinecraftServerPlayer getPlayerByInstance(@NotNull Object instance);

    Optional<MinecraftServerPlayer> getPlayerByName(@NotNull String name);

    Optional<MinecraftServerPlayer> getPlayerById(@NotNull UUID playerId);

    Optional<MinecraftGameProfile> getGameProfile(@NotNull UUID playerId);

    Optional<MinecraftGameProfile> getGameProfile(@NotNull String name);

    @NotNull Collection<MinecraftServerPlayer> getPlayers();

    @NotNull MinecraftServerEntity getEntity(@NotNull Object instance);

    @NotNull MinecraftCommandManager getCommandManager();

    @NotNull PermissionsManager getPermissionsManager();
}

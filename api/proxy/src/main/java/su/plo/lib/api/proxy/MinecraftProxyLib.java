package su.plo.lib.api.proxy;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.command.MinecraftProxyCommand;
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;
import su.plo.lib.api.server.MinecraftCommonServerLib;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.api.server.permission.PermissionsManager;
import su.plo.voice.api.event.EventBus;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MinecraftProxyLib extends MinecraftCommonServerLib {

    Optional<MinecraftProxyPlayer> getPlayerById(@NotNull UUID playerId);

    @NotNull MinecraftProxyPlayer getPlayerByInstance(@NotNull Object instance);

    Optional<MinecraftProxyPlayer> getPlayerByName(@NotNull String name);

    Collection<MinecraftProxyPlayer> getPlayers();

    Optional<MinecraftProxyServerInfo> getServerByName(@NotNull String name);

    @NotNull MinecraftProxyServerInfo getServerInfoByServerInstance(@NotNull Object instance);

    Collection<MinecraftProxyServerInfo> getServers();

    int getPort();

    @NotNull MinecraftCommandManager<MinecraftProxyCommand> getCommandManager();

    @NotNull PermissionsManager getPermissionsManager();
}

package su.plo.lib.api.proxy.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.connection.MinecraftProxyConnection;
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection;
import su.plo.lib.api.server.player.MinecraftServerPlayer;

import java.util.Optional;

public interface MinecraftProxyPlayer extends MinecraftServerPlayer, MinecraftProxyConnection {

    @NotNull MinecraftTabList getTabList();

    Optional<MinecraftProxyServerConnection> getServer();
}

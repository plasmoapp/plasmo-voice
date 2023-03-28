package su.plo.lib.api.proxy.player;

import su.plo.lib.api.proxy.connection.MinecraftProxyConnection;
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection;
import su.plo.lib.api.server.player.MinecraftServerPlayer;

import java.util.Optional;

public interface MinecraftProxyPlayer extends MinecraftServerPlayer, MinecraftProxyConnection {

    Optional<MinecraftProxyServerConnection> getServer();
}

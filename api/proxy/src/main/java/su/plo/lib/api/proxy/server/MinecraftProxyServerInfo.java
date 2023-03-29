package su.plo.lib.api.proxy.server;

import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public interface MinecraftProxyServerInfo {

    @NotNull String getName();

    @NotNull SocketAddress getAddress();

    int getPlayerCount();
}

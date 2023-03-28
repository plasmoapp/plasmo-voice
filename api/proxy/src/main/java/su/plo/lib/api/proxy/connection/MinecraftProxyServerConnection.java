package su.plo.lib.api.proxy.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;

public interface MinecraftProxyServerConnection extends MinecraftProxyConnection {

    @NotNull MinecraftProxyServerInfo getServerInfo();
}

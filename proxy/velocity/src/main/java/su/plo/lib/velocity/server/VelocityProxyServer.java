package su.plo.lib.velocity.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.lib.api.proxy.server.MinecraftProxyServer;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;

@RequiredArgsConstructor
public final class VelocityProxyServer implements MinecraftProxyServer {

    private final MinecraftProxyLib minecraftProxy;
    @Getter
    private final RegisteredServer instance;

    @Override
    public @NotNull MinecraftProxyServerInfo getInfo() {
        return minecraftProxy.getServerInfoByServerInstance(instance);
    }
}

package su.plo.lib.velocity.connection;

import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection;
import su.plo.lib.api.proxy.server.MinecraftProxyServer;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;
import su.plo.lib.velocity.server.VelocityProxyServer;

public final class VelocityProxyServerConnection implements MinecraftProxyServerConnection {

    @Getter
    private final ServerConnection instance;
    private final VelocityProxyServer server;

    public VelocityProxyServerConnection(@NotNull MinecraftProxyLib minecraftProxy,
                                         @NotNull ServerConnection instance) {
        this.instance = instance;
        this.server = new VelocityProxyServer(minecraftProxy, instance.getServer());
    }

    @Override
    public void sendPacket(@NotNull String channel, byte[] data) {
        instance.sendPluginMessage(MinecraftChannelIdentifier.from(channel), data);
    }

    @Override
    public @NotNull MinecraftProxyServer getServer() {
        return server;
    }

    @Override
    public @NotNull MinecraftProxyServerInfo getServerInfo() {
        return server.getInfo();
    }
}

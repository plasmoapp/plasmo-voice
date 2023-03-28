package su.plo.lib.velocity.connection;

import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;

public final class VelocityProxyServerConnection implements MinecraftProxyServerConnection {

    private final MinecraftProxyLib minecraftProxy;
    @Getter
    private final ServerConnection instance;

    public VelocityProxyServerConnection(@NotNull MinecraftProxyLib minecraftProxy,
                                         @NotNull ServerConnection instance) {
        this.minecraftProxy = minecraftProxy;
        this.instance = instance;
    }

    @Override
    public void sendPacket(@NotNull String channel, byte[] data) {
        instance.sendPluginMessage(MinecraftChannelIdentifier.from(channel), data);
    }

    @Override
    public @NotNull MinecraftProxyServerInfo getServerInfo() {
        return minecraftProxy.getServerInfoByServerInstance(instance.getServer());
    }
}

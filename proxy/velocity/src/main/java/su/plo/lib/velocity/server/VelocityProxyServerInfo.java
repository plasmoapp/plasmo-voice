package su.plo.lib.velocity.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;

import java.net.SocketAddress;
import java.util.Objects;

@ToString(onlyExplicitlyIncluded = true)
public final class VelocityProxyServerInfo implements MinecraftProxyServerInfo {

    private final RegisteredServer registeredServer;
    @Getter
    @ToString.Include
    private final ServerInfo instance;

    public VelocityProxyServerInfo(@NotNull RegisteredServer registeredServer) {
        this.registeredServer = registeredServer;
        this.instance = registeredServer.getServerInfo();
    }

    @Override
    public @NotNull String getName() {
        return instance.getName();
    }

    @Override
    public @NotNull SocketAddress getAddress() {
        return instance.getAddress();
    }

    @Override
    public int getPlayerCount() {
        return registeredServer.getPlayersConnected().size();
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof ServerInfo && Objects.equals(instance, o)) ||
                (o instanceof VelocityProxyServerInfo && Objects.equals(instance, ((VelocityProxyServerInfo) o).instance));
    }

    @Override
    public int hashCode() {
        return instance.hashCode();
    }
}

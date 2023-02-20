package su.plo.lib.velocity.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;

import java.util.Objects;

@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public final class VelocityProxyServerInfo implements MinecraftProxyServerInfo {

    @Getter
    @ToString.Include
    private final ServerInfo instance;

    @Override
    public @NotNull String getName() {
        return instance.getName();
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

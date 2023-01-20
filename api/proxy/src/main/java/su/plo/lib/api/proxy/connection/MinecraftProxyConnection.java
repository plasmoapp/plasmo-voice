package su.plo.lib.api.proxy.connection;

import org.jetbrains.annotations.NotNull;

public interface MinecraftProxyConnection {

    void sendPacket(@NotNull String channel, byte[] data);
}

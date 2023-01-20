package su.plo.voice.api.proxy.server;

import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public interface RemoteServer {

    boolean isAesEncryptionKeySet();

    @NotNull String getName();

    @NotNull InetSocketAddress getAddress();
}

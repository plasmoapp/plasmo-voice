package su.plo.voice.api.proxy.server;

import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * Represents a backend remote voice server.
 */
public interface RemoteServer {

    /**
     * Checks if an AES encryption key is set for this remote server.
     *
     * @return {@code true} if an AES encryption key is set, {@code false} otherwise.
     */
    boolean isAesEncryptionKeySet();

    /**
     * Gets the name of the remote server.
     *
     * @return The name of the remote server.
     */
    @NotNull String getName();

    /**
     * Gets the address of the remote server.
     *
     * @return The address of the remote server.
     */
    @NotNull InetSocketAddress getAddress();
}

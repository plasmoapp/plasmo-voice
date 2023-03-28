package su.plo.voice.api.proxy.server;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This manager represents additional ip:port mappings for connection to backend UDP server
 *
 * <p>
 *     By default, proxy's backend address will be used for connection to backend UDP server
 * </p>
 */
public interface RemoteServerManager {

    /**
     * Gets the {@link RemoteServer} by name
     *
     * @param name server name
     *
     * @return {@link RemoteServer}
     */
    Optional<RemoteServer> getServer(@NotNull String name);

    /**
     * Registers the {@link RemoteServer}
     *
     * @param server {@link RemoteServer}
     */
    void register(@NotNull RemoteServer server);

    /**
     * Unregisters the {@link RemoteServer}
     *
     * @param server {@link RemoteServer}
     */
    void unregister(@NotNull RemoteServer server);

    /**
     * Unregisters the {@link RemoteServer}
     *
     * @param name server name
     */
    void unregister(@NotNull String name);

    /**
     * Clears all remote servers
     */
    void clear();
}

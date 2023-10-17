package su.plo.voice.api.proxy.server;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Manages remote backend voice servers.
 */
public interface RemoteServerManager {

    /**
     * Gets the {@link RemoteServer} by name.
     *
     * @param name The name of the server.
     * @return An optional containing the {@link RemoteServer} if found, or empty if not found.
     */
    Optional<RemoteServer> getServer(@NotNull String name);

    /**
     * Registers a {@link RemoteServer}.
     *
     * @param server The {@link RemoteServer} to register.
     */
    void register(@NotNull RemoteServer server);

    /**
     * Unregisters a {@link RemoteServer}.
     *
     * @param server The {@link RemoteServer} to unregister.
     */
    void unregister(@NotNull RemoteServer server);

    /**
     * Unregisters a {@link RemoteServer} by the server name.
     *
     * @param name The name of the server to unregister.
     */
    void unregister(@NotNull String name);

    /**
     * Removes all registered remote servers.
     */
    void clear();
}

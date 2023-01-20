package su.plo.voice.api.proxy.server;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface RemoteServerManager {

    /**
     * Gets the all UDP remote servers
     *
     * @return Collection of {@link RemoteServer}
     */
    Collection<RemoteServer> getAllServers();

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

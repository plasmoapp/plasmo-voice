package su.plo.voice.proxy.server;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.proxy.server.RemoteServerManager;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class VoiceRemoteServerManager implements RemoteServerManager {

    private final Map<String, RemoteServer> servers = Maps.newConcurrentMap();

    @Override
    public Optional<RemoteServer> getServer(@NotNull String name) {
        return Optional.ofNullable(servers.get(name));
    }

    @Override
    public Collection<RemoteServer> getAllServers() {
        return servers.values();
    }

    @Override
    public void register(@NotNull RemoteServer server) {
        servers.put(server.getName(), server);
    }

    @Override
    public void unregister(@NotNull RemoteServer server) {
        unregister(server.getName());
    }

    @Override
    public void unregister(@NotNull String name) {
        servers.remove(name);
    }

    @Override
    public void clear() {
        servers.clear();
    }
}

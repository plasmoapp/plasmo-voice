package su.plo.voice.proxy.server;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.proxy.server.RemoteServerManager;
import su.plo.voice.proxy.BaseVoiceProxy;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public final class VoiceRemoteServerManager implements RemoteServerManager {

    private final BaseVoiceProxy voiceProxy;

    private final Map<String, RemoteServer> servers = Maps.newConcurrentMap();

    @Override
    public Optional<RemoteServer> getServer(@NotNull String name) {
        if (!servers.containsKey(name)) {
            return voiceProxy.getMinecraftServer()
                    .getServerByName(name)
                    // only InetSocketAddress is supported
                    .filter(serverInfo -> serverInfo.getAddress() instanceof InetSocketAddress)
                    .map(this::registerByServerInfo);
        }

        return Optional.ofNullable(servers.get(name));
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

    private RemoteServer getByServerInfo(@NotNull MinecraftProxyServerInfo serverInfo) {
        RemoteServer remoteServer = servers.get(serverInfo.getName());
        if (remoteServer != null) return remoteServer;

        return registerByServerInfo(serverInfo);
    }

    private RemoteServer registerByServerInfo(@NotNull MinecraftProxyServerInfo serverInfo) {
        if (!(serverInfo.getAddress() instanceof InetSocketAddress))
            throw new IllegalArgumentException("only InetSocketAddress is supported");

        VoiceRemoteServer remoteServer = new VoiceRemoteServer(serverInfo.getName(), (InetSocketAddress) serverInfo.getAddress());
        servers.put(serverInfo.getName(), remoteServer);
        return remoteServer;
    }
}

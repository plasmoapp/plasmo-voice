package su.plo.voice.proxy.server;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.event.player.McPlayerQuitEvent;
import su.plo.slib.api.proxy.connection.McProxyServerConnection;
import su.plo.slib.api.proxy.player.McProxyPlayer;
import su.plo.slib.api.proxy.server.McProxyServerInfo;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.proxy.server.RemoteServerManager;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.proxy.util.AddressUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

public final class VoiceRemoteServerManager implements RemoteServerManager {

    private final BaseVoiceProxy voiceProxy;

    private final Map<String, RemoteServer> servers = Maps.newConcurrentMap();

    public VoiceRemoteServerManager(@NotNull BaseVoiceProxy voiceProxy) {
        this.voiceProxy = voiceProxy;

        McPlayerQuitEvent.INSTANCE.registerListener(player -> {
            McProxyPlayer proxyPlayer = (McProxyPlayer) player;
            McProxyServerConnection playerServer = proxyPlayer.getServer();

            if (playerServer != null) {
                resetServerAesState(playerServer.getServerInfo());
                return;
            }

            voiceProxy.getMinecraftServer().getServers().forEach(this::resetServerAesState);
        });
    }

    @Override
    public Optional<RemoteServer> getServer(@NotNull String name) {
        if (!servers.containsKey(name)) {
            return Optional.ofNullable(voiceProxy.getMinecraftServer().getServerInfoByName(name))
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

    private void resetServerAesState(@NotNull McProxyServerInfo server) {
        if (server.getPlayerCount() > 0) return;
        getServer(server.getName())
                .filter(RemoteServer::isAesEncryptionKeySet)
                .ifPresent(remoteServer -> {
                    BaseVoice.DEBUG_LOGGER.log("Reset AES encryption state for {}", remoteServer);
                    ((VoiceRemoteServer) remoteServer).setAesEncryptionKeySet(false);
                });
    }

    private RemoteServer getByServerInfo(@NotNull McProxyServerInfo serverInfo) {
        RemoteServer remoteServer = servers.get(serverInfo.getName());
        if (remoteServer != null) return remoteServer;

        return registerByServerInfo(serverInfo);
    }

    private RemoteServer registerByServerInfo(@NotNull McProxyServerInfo serverInfo) {
        if (!(serverInfo.getAddress() instanceof InetSocketAddress))
            throw new IllegalArgumentException("only InetSocketAddress is supported");

        InetSocketAddress serverAddress = AddressUtil.resolveAddress((InetSocketAddress) serverInfo.getAddress());
        VoiceRemoteServer remoteServer = new VoiceRemoteServer(serverInfo.getName(), serverAddress);
        servers.put(serverInfo.getName(), remoteServer);
        return remoteServer;
    }
}

package su.plo.voice.proxy.server;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection;
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer;
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo;
import su.plo.lib.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.proxy.server.RemoteServerManager;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.proxy.event.player.McProxyServerConnectedEvent;
import su.plo.voice.proxy.util.AddressUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

public final class VoiceRemoteServerManager implements RemoteServerManager {

    private final BaseVoiceProxy voiceProxy;

    private final Map<String, RemoteServer> servers = Maps.newConcurrentMap();

    public VoiceRemoteServerManager(@NotNull BaseVoiceProxy voiceProxy) {
        this.voiceProxy = voiceProxy;

        PlayerQuitEvent.INSTANCE.registerListener(player -> {
            MinecraftProxyPlayer proxyPlayer = (MinecraftProxyPlayer) player;
            Optional<MinecraftProxyServerConnection> playerServer = proxyPlayer.getServer();

            if (playerServer.isPresent()) {
                resetServerAesState(playerServer.get().getServerInfo());
                return;
            }

            voiceProxy.getMinecraftServer().getServers().forEach(this::resetServerAesState);
        });

        McProxyServerConnectedEvent.INSTANCE.registerListener((player, previousServer) -> {
            if (previousServer == null) return;
            resetServerAesState(previousServer);
        });
    }

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

    private void resetServerAesState(@NotNull MinecraftProxyServerInfo server) {
        if (server.getPlayerCount() > 0) return;
        Optional.ofNullable(servers.get(server.getName()))
                .filter(RemoteServer::isAesEncryptionKeySet)
                .ifPresent(remoteServer -> {
                    BaseVoice.DEBUG_LOGGER.log("Reset AES encryption state for {}", remoteServer);
                    ((VoiceRemoteServer) remoteServer).setAesEncryptionKeySet(false);
                });
    }

    private RemoteServer getByServerInfo(@NotNull MinecraftProxyServerInfo serverInfo) {
        RemoteServer remoteServer = servers.get(serverInfo.getName());
        if (remoteServer != null) return remoteServer;

        return registerByServerInfo(serverInfo);
    }

    private RemoteServer registerByServerInfo(@NotNull MinecraftProxyServerInfo serverInfo) {
        if (!(serverInfo.getAddress() instanceof InetSocketAddress))
            throw new IllegalArgumentException("only InetSocketAddress is supported");

        InetSocketAddress serverAddress = AddressUtil.resolveAddress((InetSocketAddress) serverInfo.getAddress());
        VoiceRemoteServer remoteServer = new VoiceRemoteServer(serverInfo.getName(), serverAddress);
        servers.put(serverInfo.getName(), remoteServer);
        return remoteServer;
    }
}

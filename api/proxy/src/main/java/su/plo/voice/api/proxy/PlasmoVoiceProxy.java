package su.plo.voice.api.proxy;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.voice.api.proxy.audio.source.ProxySourceManager;
import su.plo.voice.api.proxy.config.ProxyConfig;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.server.RemoteServerManager;
import su.plo.voice.api.proxy.socket.UdpProxyServer;
import su.plo.voice.api.server.PlasmoCommonVoiceServer;
import su.plo.voice.api.server.player.VoicePlayerManager;

import java.util.Optional;

/**
 * The Plasmo Server Proxy API
 */
public interface PlasmoVoiceProxy extends PlasmoCommonVoiceServer {

    /**
     * Gets the {@link MinecraftProxyLib}
     *
     * @return {@link MinecraftProxyLib}
     */
    @NotNull MinecraftProxyLib getMinecraftServer();

    /**
     * Gets the {@link UdpProxyConnectionManager}
     * <p>
     * This manager can be used to broadcast or manage udp connections
     *
     * @return {@link UdpProxyConnectionManager}
     */
    @NotNull UdpProxyConnectionManager getUdpConnectionManager();

    /**
     * Gets the {@link VoicePlayerManager}
     * <p>
     * This manager can be used to get voice players
     *
     * @return {@link VoicePlayerManager}
     */
    @NotNull VoicePlayerManager<VoiceProxyPlayer> getPlayerManager();

    /**
     * Gets the {@link UdpProxyServer}
     * <p>
     * This server can be used to broadcast or manage udp connections
     *
     * @return {@link UdpProxyServer}
     */
    @NotNull RemoteServerManager getRemoteServerManager();

    /**
     * Gets the {@link UdpProxyServer}
     *
     * @return {@link UdpProxyServer}
     */
    Optional<UdpProxyServer> getUdpProxyServer();

    /**
     * Gets the {@link ProxySourceManager}
     *
     * @return {@link ProxySourceManager}
     */
    @NotNull ProxySourceManager getSourceManager();

    /**
     * Gets the {@link ProxyConfig}
     *
     * @return {@link ProxyConfig}
     */
    ProxyConfig getConfig();
}

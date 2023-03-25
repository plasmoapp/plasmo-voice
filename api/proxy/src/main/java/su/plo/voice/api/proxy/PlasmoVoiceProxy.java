package su.plo.voice.api.proxy;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.voice.api.addon.AddonsLoader;
import su.plo.voice.api.addon.ServerAddonsLoader;
import su.plo.voice.api.proxy.config.ProxyConfig;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.server.RemoteServerManager;
import su.plo.voice.api.proxy.socket.UdpProxyServer;
import su.plo.voice.api.server.PlasmoBaseVoiceServer;
import su.plo.voice.api.server.player.VoiceProxyPlayerManager;

import java.util.Optional;

/**
 * The Plasmo Server Proxy API
 */
public interface PlasmoVoiceProxy extends PlasmoBaseVoiceServer {

    /**
     * Gets the server's addons loader
     *
     * <p>Use this method to get the addons loader for loading server/proxy addons from Spigot/Forge/Fabric</p>
     *
     * @return the addons loader
     */
    static AddonsLoader getAddonsLoader() {
        return ServerAddonsLoader.INSTANCE;
    }

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
     * Gets the {@link VoiceProxyPlayerManager}
     * <p>
     * This manager can be used to get voice players
     *
     * @return {@link VoiceProxyPlayerManager}
     */
    @NotNull VoiceProxyPlayerManager getPlayerManager();

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
     * Gets the {@link ProxyConfig}
     *
     * @return {@link ProxyConfig}
     */
    ProxyConfig getConfig();
}

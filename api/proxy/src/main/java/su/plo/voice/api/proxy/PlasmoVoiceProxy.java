package su.plo.voice.api.proxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.proxy.McProxyLib;
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
 * The Plasmo Voice Proxy API.
 */
public interface PlasmoVoiceProxy extends PlasmoBaseVoiceServer {

    /**
     * Gets the proxy's addons loader.
     *
     * <p>Use this method to get the addons loader for loading proxy addons from BungeeCord/Velocity.</p>
     *
     * @return The addons loader.
     */
    static AddonsLoader getAddonsLoader() {
        return ServerAddonsLoader.INSTANCE;
    }

    /**
     * Gets the {@link McProxyLib}.
     *
     * @return The {@link McProxyLib}.
     */
    @NotNull McProxyLib getMinecraftServer();

    /**
     * Gets the {@link UdpProxyConnectionManager}.
     * <p>
     *     This manager can be used to broadcast or manage UDP connections.
     * </p>
     *
     * @return The {@link UdpProxyConnectionManager}.
     */
    @NotNull UdpProxyConnectionManager getUdpConnectionManager();

    /**
     * Gets the {@link VoiceProxyPlayerManager}.
     * <p>
     *     This manager can be used to get voice players.
     * </p>
     *
     * @return The {@link VoiceProxyPlayerManager}.
     */
    @NotNull VoiceProxyPlayerManager getPlayerManager();

    /**
     * Gets the {@link RemoteServerManager}.
     *
     * @return The {@link RemoteServerManager}.
     */
    @NotNull RemoteServerManager getRemoteServerManager();

    /**
     * Gets the {@link UdpProxyServer}.
     *
     * @return The {@link UdpProxyServer}.
     */
    Optional<UdpProxyServer> getUdpProxyServer();

    /**
     * Gets the {@link ProxyConfig}.
     *
     * @return The {@link ProxyConfig} or null if proxy is not initialized yet.
     */
    @Nullable ProxyConfig getConfig();
}

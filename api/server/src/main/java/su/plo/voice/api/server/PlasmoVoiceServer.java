package su.plo.voice.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.audio.source.ServerSourceManager;
import su.plo.voice.api.server.config.ServerConfig;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.api.server.socket.UdpServer;

import java.util.Optional;

/**
 * The Plasmo Client Server API
 */
public interface PlasmoVoiceServer extends PlasmoVoice {

    /**
     * Gets the {@link MinecraftServerLib}
     *
     * @return {@link MinecraftServerLib}
     */
    @NotNull MinecraftServerLib getMinecraftServer();

    /**
     * Gets the {@link VoicePlayerManager}
     *
     * This manager can be used to get voice players
     *
     * @return {@link VoicePlayerManager}
     */
    @NotNull VoicePlayerManager getPlayerManager();

    /**
     * Gets the {@link MuteManager}
     *
     * This manager can be used to mute or unmute players voice
     *
     *
     */
    @NotNull MuteManager getMuteManager();

    /**
     * Gets the {@link ServerSourceManager}
     *
     * @return {@link ServerSourceManager}
     */
    @NotNull ServerSourceManager getSourceManager();

    /**
     * Gets the {@link ServerActivationManager}
     *
     * @return {@link ServerActivationManager}
     */
    @NotNull ServerActivationManager getActivationManager();

    /**
     * Gets the {@link ServerSourceLineManager}
     *
     * @return {@link ServerSourceLineManager}
     */
    @NotNull ServerSourceLineManager getSourceLineManager();

    /**
     * Gets the {@link TcpServerConnectionManager}
     *
     * This manager can be used to broadcast to tcp connections
     *
     * @return {@link TcpServerConnectionManager}
     */
    @NotNull TcpServerConnectionManager getTcpConnectionManager();

    /**
     * Gets the {@link UdpServerConnectionManager}
     *
     * This manager can be used to broadcast or manage udp connections
     *
     * @return {@link UdpServerConnectionManager}
     */
    @NotNull UdpServerConnectionManager getUdpConnectionManager();

    /**
     * Gets the {@link UdpServer}
     *
     * @return {@link UdpServer}
     */
    Optional<UdpServer> getUdpServer();

    /**
     * Gets the {@link ServerConfig}
     *
     * @return {@link ServerConfig} if its loaded
     */
    Optional<ServerConfig> getConfig();
}

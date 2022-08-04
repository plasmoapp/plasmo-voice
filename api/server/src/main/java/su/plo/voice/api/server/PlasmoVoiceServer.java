package su.plo.voice.api.server;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.socket.UdpServer;

import java.util.Optional;

/**
 * The Plasmo Client Server API
 */
public interface PlasmoVoiceServer extends PlasmoVoice {

    /**
     * Gets the {@link PlayerManager}
     *
     * This manager can be used to get voice players
     *
     * @return the player manager
     */
    @NotNull PlayerManager getPlayerManager();

    /**
     * Gets the {@link UdpServerConnectionManager}
     *
     * This manager can be used to broadcast to tcp connections
     *
     * @return the connection manager
     */
    @NotNull TcpServerConnectionManager getTcpConnectionManager();

    /**
     * Gets the {@link UdpServerConnectionManager}
     *
     * This manager can be used to broadcast or manage udp connections
     *
     * @return the connection manager
     */
    @NotNull UdpServerConnectionManager getUdpConnectionManager();

    /**
     * Get the {@link UdpServer}
     *
     * @return the udp server
     */
    Optional<UdpServer> getUdpServer();
}

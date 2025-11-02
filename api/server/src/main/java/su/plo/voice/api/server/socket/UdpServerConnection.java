package su.plo.voice.api.server.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;

/**
 * Identified player UDP connection.
 */
public interface UdpServerConnection extends UdpConnection {

    /**
     * Gets the {@link VoiceServerPlayer} associated with this connection.
     *
     * @return The associated {@link VoiceServerPlayer}.
     */
    @NotNull VoiceServerPlayer getPlayer();

    /**
     * Gets the timestamp of the last received keep-alive packet from this connection.
     *
     * @return The timestamp of the last received keep-alive packet.
     */
    long getKeepAlive();

    /**
     * Gets the timestamp of the last sent keep-alive packet to this connection.
     *
     * @return The timestamp of the last sent keep-alive packet.
     */
    long getSentKeepAlive();

    /**
     * Sets the timestamp of the last sent keep-alive packet to this connection.
     *
     * @param keepAlive The timestamp of the last sent keep-alive packet.
     */
    void setSentKeepAlive(long keepAlive);

    /**
     * Gets the timestamp of the next scheduled keep alive packet.
     *
     * @return The timestamp of the next scheduled keep-alive packet.
     */
    long getNextKeepAlive();

    /**
     * Sets the timestamp of the next scheduled keep alive packet.
     *
     * @param keepAlive The timestamp of the next scheduled keep alive packet.
     */
    void setNextKeepAlive(long keepAlive);

    /**
     * Gets the timestamp of the last received packet from this connection.
     *
     * @return The timestamp of the last received packet.
     */
    long getLastReceivedPacketTimestamp();
}

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
     * Gets the timestamp of the last received keep-alive signal from this connection.
     *
     * @return The timestamp of the last received keep-alive signal.
     */
    long getKeepAlive();

    /**
     * Gets the timestamp of the last sent keep-alive signal to this connection.
     *
     * @return The timestamp of the last sent keep-alive signal.
     */
    long getSentKeepAlive();

    /**
     * Sets the timestamp of the last sent keep-alive signal to this connection.
     *
     * @param keepAlive The timestamp of the last sent keep-alive signal.
     */
    void setSentKeepAlive(long keepAlive);
}

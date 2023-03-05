package su.plo.voice.api.server.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;

public interface UdpServerConnection extends UdpConnection {

    /**
     * @return connection's {@link VoiceServerPlayer}
     */
    @NotNull VoiceServerPlayer getPlayer();

    /**
     * @return last received keep alive timestamp
     */
    long getKeepAlive();

    /**
     * @return last sent keep alive timestamp
     */
    long getSentKeepAlive();

    /**
     * Sets the last sent keep alive timestamp
     */
    void setSentKeepAlive(long keepAlive);
}

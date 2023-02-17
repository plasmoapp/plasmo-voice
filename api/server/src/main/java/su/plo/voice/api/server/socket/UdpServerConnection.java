package su.plo.voice.api.server.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;

// todo: doc
public interface UdpServerConnection extends UdpConnection {

    @NotNull VoiceServerPlayer getPlayer();

    long getKeepAlive();

    long getSentKeepAlive();

    void setSentKeepAlive(long keepAlive);
}

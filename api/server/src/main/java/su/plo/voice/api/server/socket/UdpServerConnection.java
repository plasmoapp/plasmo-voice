package su.plo.voice.api.server.socket;

import su.plo.voice.api.server.player.VoiceServerPlayer;

// todo: doc
public interface UdpServerConnection extends UdpConnection {

    VoiceServerPlayer getPlayer();

    long getKeepAlive();

    long getSentKeepAlive();

    void setSentKeepAlive(long keepAlive);
}

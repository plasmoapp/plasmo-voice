package su.plo.voice.api.server.socket;

import su.plo.voice.api.server.player.VoiceServerPlayer;

// todo: doc
public interface UdpServerConnection extends UdpConnection<VoiceServerPlayer> {

    long getKeepAlive();

    long getSentKeepAlive();

    void setSentKeepAlive(long keepAlive);
}

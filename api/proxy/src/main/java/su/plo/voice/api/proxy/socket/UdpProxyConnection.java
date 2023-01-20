package su.plo.voice.api.proxy.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.server.socket.UdpConnection;

import java.util.UUID;

// todo: doc
public interface UdpProxyConnection extends UdpConnection<VoiceProxyPlayer> {

    @NotNull UUID getRemoteSecret();

    void setRemoteSecret(@NotNull UUID remoteSecret);

    RemoteServer getRemoteServer();

    void setRemoteServer(@NotNull RemoteServer remoteServer);
}

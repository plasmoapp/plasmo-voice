package su.plo.voice.api.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.udp.clientbound.ClientPacketUdpHandler;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UdpConnectionManager<P extends VoicePlayer<?>, C extends UdpConnection<P>>
        extends ConnectionManager<ClientPacketUdpHandler, P> {

    Optional<C> getConnectionByPlayerId(@NotNull UUID playerId);

    Optional<C> getConnectionBySecret(@NotNull UUID secret);

    Collection<C> getConnections();
}

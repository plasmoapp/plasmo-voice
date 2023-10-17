package su.plo.voice.api.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.udp.clientbound.ClientPacketUdpHandler;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UdpConnectionManager<P extends VoicePlayer, C extends UdpConnection>
        extends PacketManager<ClientPacketUdpHandler, P> {

    /**
     * @return connection by player uuid
     */
    Optional<C> getConnectionByPlayerId(@NotNull UUID playerId);

    /**
     * @return connection by secret
     */
    Optional<C> getConnectionBySecret(@NotNull UUID secret);

    /**
     * @return collection of all connections
     */
    Collection<C> getConnections();
}

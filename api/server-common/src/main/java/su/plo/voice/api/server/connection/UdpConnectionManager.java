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
     * Gets the UDP connection by player unique identifier.
     *
     * @return An optional containing the UDP connection if found, otherwise empty.
     */
    Optional<C> getConnectionByPlayerId(@NotNull UUID playerId);

    /**
     * Gets the UDP connection by secret.
     *
     * @return An optional containing the UDP connection if found, otherwise empty.
     */
    Optional<C> getConnectionBySecret(@NotNull UUID secret);

    /**
     * Gets the collection of all UDP connections.
     *
     * @return A collection of UDP connections.
     */
    Collection<C> getConnections();
}

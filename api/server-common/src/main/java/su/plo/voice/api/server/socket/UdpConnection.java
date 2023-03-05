package su.plo.voice.api.server.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * Identified UDP connection
 */
public interface UdpConnection {

    /**
     * @return connection's secret
     */
    @NotNull UUID getSecret();

    /**
     * @return connection's {@link VoicePlayer}
     */
    @NotNull VoicePlayer getPlayer();

    /**
     * Gets the connection's remote address
     * <br/>
     * Can be changed by UDP packet handler once player's remote address was changed
     */
    @NotNull InetSocketAddress getRemoteAddress();

    /**
     * Sets the connection's remote address
     */
    void setRemoteAddress(@NotNull InetSocketAddress remoteAddress);

    /**
     * Sends the packet to UDP connection
     */
    void sendPacket(Packet<?> packet);

    /**
     * Handles UDP packet received from the player
     */
    void handlePacket(Packet<ServerPacketUdpHandler> packet);

    /**
     * Disconnects the UDP connection and set connected state to false
     */
    void disconnect();

    /**
     * @return true if connection is alive
     */
    boolean isConnected();
}

package su.plo.voice.api.server.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * Identified UDP connection.
 */
public interface UdpConnection {

    /**
     * Gets the connection's secret.
     *
     * @return The connection's secret.
     */
    @NotNull UUID getSecret();

    /**
     * Gets the {@link VoicePlayer} associated with this connection.
     *
     * @return The associated {@link VoicePlayer}.
     */
    @NotNull VoicePlayer getPlayer();


    /**
     * Gets the connection's remote address.
     * <p>
     *     Note:
     *     The remote address can be changed by the UDP packet handler
     *     once the player's remote address has been changed.
     * </p>
     *
     * @return The remote address of the connection as an {@link InetSocketAddress}.
     */
    @NotNull InetSocketAddress getRemoteAddress();

    /**
     * Sets the connection's remote address.
     *
     * @param remoteAddress The new remote address to set.
     */
    void setRemoteAddress(@NotNull InetSocketAddress remoteAddress);

    /**
     * Sends a packet to the UDP connection.
     *
     * @param packet The packet to send.
     */
    void sendPacket(Packet<?> packet);

    /**
     * Handles a UDP packet received from the player.
     *
     * @param packet The UDP packet to handle.
     */
    void handlePacket(Packet<ServerPacketUdpHandler> packet);

    /**
     * Disconnects the UDP connection and sets its connected state to false.
     */
    void disconnect();

    /**
     * Checks if the connection is alive.
     *
     * @return {@code true} if the connection is alive, otherwise {@code false}.
     */
    boolean isConnected();
}

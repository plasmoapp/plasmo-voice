package su.plo.voice.api.client.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.proto.packets.Packet;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

/**
 * UDP client for receiving and sending audio packets to the UDP voice server.
 */
public interface UdpClient {

    /**
     * Connects the UDP client to the specified IP address and port.
     *
     * @param ip   The IP address to connect to.
     * @param port The port to connect to.
     */
    void connect(@NotNull String ip, int port);

    /**
     * Closes the UDP client with the specified reason.
     *
     * @param reason The reason for closing the UDP client.
     */
    void close(@NotNull UdpClientClosedEvent.Reason reason);

    /**
     * Sends a packet through the UDP client.
     *
     * @param packet The packet to send.
     */
    void sendPacket(Packet<?> packet);

    /**
     * Gets the secret associated with the UDP client.
     *
     * @return The secret.
     */
    @NotNull UUID getSecret();

    /**
     * Gets the remote address to which the UDP client is connected.
     *
     * @return An optional InetSocketAddress representing the remote address, if available.
     */
    Optional<InetSocketAddress> getRemoteAddress();

    /**
     * Checks if the UDP client is closed.
     *
     * @return {@code true} if the UDP client is closed, {@code false} otherwise.
     */
    boolean isClosed();

    /**
     * Checks if the UDP client is connected.
     *
     * @return {@code true} if the UDP client is connected, {@code false} otherwise.
     */
    boolean isConnected();

    /**
     * Checks if the UDP client has timed out.
     *
     * @return {@code true} if the UDP client has timed out, {@code false} otherwise.
     */
    boolean isTimedOut();
}

package su.plo.voice.api.server.socket;

import su.plo.voice.api.server.event.socket.UdpServerCreateEvent;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Represents a UDP server for handling UDP connections.
 * <br/>
 * Implementation of the {@link UdpServer} can be changed by replacing in {@link UdpServerCreateEvent}.
 */
public interface UdpServer {

    /**
     * Starts the UDP server.
     *
     * @param ip   The IP address on which the server should listen.
     * @param port The port on which the server should listen.
     */
    void start(String ip, int port);

    /**
     * Stops the UDP server.
     */
    void stop();

    /**
     * Gets the remote address to which the UDP server is bound.
     *
     * @return An optional containing the remote address, or empty if the server is not started yet.
     */
    Optional<InetSocketAddress> getRemoteAddress();
}

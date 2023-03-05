package su.plo.voice.api.server.socket;

import su.plo.voice.api.server.event.socket.UdpServerCreateEvent;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * UDP server for accept and handle UDP connections
 * <br/>
 * Implementation of the {@link UdpServer} can be changed by replacing in
 * {@link UdpServerCreateEvent}
 */
public interface UdpServer {

    /**
     * Starts the UDP server on specified ip and port
     */
    void start(String ip, int port);

    /**
     * Stops the UDP server
     */
    void stop();

    /**
     * Gets the server's bind address
     * @return bind address if server is started
     */
    Optional<InetSocketAddress> getRemoteAddress();
}

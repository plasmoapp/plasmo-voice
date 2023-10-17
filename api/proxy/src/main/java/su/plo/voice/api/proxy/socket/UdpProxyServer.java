package su.plo.voice.api.proxy.socket;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Represents a UDP proxy server for handling UDP connections.
 */
public interface UdpProxyServer {

    /**
     * Starts the UDP proxy server.
     *
     * @param ip   The IP address on which the server should listen.
     * @param port The port on which the server should listen.
     */
    void start(String ip, int port);

    /**
     * Stops the UDP proxy server.
     */
    void stop();

    /**
     * Gets the remote address to which the UDP proxy server is bound.
     *
     * @return An optional containing the remote address, or empty if the server is not started yet.
     */
    Optional<InetSocketAddress> getRemoteAddress();
}

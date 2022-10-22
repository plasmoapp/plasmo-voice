package su.plo.voice.api.server.socket;

import java.net.InetSocketAddress;
import java.util.Optional;

// todo: doc
public interface UdpServer {

    void start(String ip, int port);

    void stop();

    Optional<InetSocketAddress> getRemoteAddress();
}

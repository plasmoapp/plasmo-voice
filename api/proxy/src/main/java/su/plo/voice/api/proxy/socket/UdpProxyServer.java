package su.plo.voice.api.proxy.socket;

import java.net.InetSocketAddress;
import java.util.Optional;

// todo: doc
public interface UdpProxyServer {

    void start(String ip, int port);

    void stop();

    Optional<InetSocketAddress> getRemoteAddress();
}

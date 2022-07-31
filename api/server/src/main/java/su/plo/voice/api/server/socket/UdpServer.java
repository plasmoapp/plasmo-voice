package su.plo.voice.api.server.socket;

// todo: doc
public interface UdpServer {
    void start(String ip, int port);

    void close();
}

package su.plo.voice.client.socket;

import su.plo.voice.common.packets.Packet;

import java.io.IOException;

public interface SocketConnection extends Runnable {
    void close();
    void send(Packet packet) throws IOException;
    void start();

    boolean isClosed();
    boolean isTimedOut();
    boolean isAuthorized();
    boolean isConnected();
}

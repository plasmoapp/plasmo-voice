package su.plo.voice.socket;

import su.plo.voice.Voice;
import su.plo.voice.common.packets.udp.AuthPacket;

import java.io.IOException;

public class SocketClientAuth extends Thread {
    private final SocketClientUDP socket;

    public SocketClientAuth(SocketClientUDP socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while(!socket.authorized && !socket.isClosed()) {
            try {
                this.socket.send(new AuthPacket(Voice.serverConfig.secret));

                Thread.sleep(2000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException ignored) {}
        }
    }
}

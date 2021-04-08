package su.plo.voice.socket;

import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;

import java.net.InetAddress;

public class SocketClientUDP {

    private final Player player;
    private final InetAddress address;
    private final int port;
    private long keepAlive;

    public SocketClientUDP(Player player, InetAddress address, int port) {
        this.player = player;
        this.address = address;
        this.port = port;
        this.keepAlive = System.currentTimeMillis();
    }

    public long getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(long time) {
        this.keepAlive = time;
    }

    public Player getPlayer() {
        return player;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void close() {
        if(SocketServerUDP.clients.containsKey(player)) {
            PlasmoVoice.logger.info("Remove client UDP: " + this.player.getName());
            SocketServerUDP.clients.remove(player);
        }
    }
}

package su.plo.voice.socket;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;

import java.net.InetAddress;

@Data
public class SocketClientUDP {
    private final Player player;
    private final InetAddress address;
    private final int port;
    private long keepAlive;
    private long sentKeepAlive;
    @Setter(AccessLevel.PROTECTED)
    private String type;

    public SocketClientUDP(Player player, String type, InetAddress address, int port) {
        this.player = player;
        this.type = type;
        this.address = address;
        this.port = port;
        this.keepAlive = System.currentTimeMillis();
    }

    public void close() {
        if (SocketServerUDP.clients.containsKey(player)) {
            if (PlasmoVoice.getInstance().getConfig().getBoolean("disable_logs")) {
                PlasmoVoice.getVoiceLogger().info("Remove client UDP: " + this.player.getName());
            }
            SocketServerUDP.clients.remove(player);
        }
    }
}

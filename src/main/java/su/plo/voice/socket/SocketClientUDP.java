package su.plo.voice.socket;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.events.PlayerVoiceDisconnectedEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Data
public class SocketClientUDP {
    private final Player player;
    private final InetSocketAddress socketAddress;
    private long keepAlive;
    private long sentKeepAlive;
    @Setter(AccessLevel.PROTECTED)
    private String type;

    public SocketClientUDP(Player player, String type, InetSocketAddress socketAddress) {
        this.player = player;
        this.type = type;
        this.socketAddress = socketAddress;
        this.keepAlive = System.currentTimeMillis();
    }

    public InetAddress getAddress() {
        return socketAddress.getAddress();
    }

    public int getPort() {
        return socketAddress.getPort();
    }

    public void close() {
        if (SocketServerUDP.clients.containsKey(player)) {
            // call event
            Bukkit.getScheduler().runTask(PlasmoVoice.getInstance(), () ->
                    Bukkit.getPluginManager().callEvent(new PlayerVoiceDisconnectedEvent(player)));

            if (!PlasmoVoice.getInstance().getConfig().getBoolean("disable_logs")) {
                PlasmoVoice.getVoiceLogger().info("Remove client UDP: " + this.player.getName());
            }
            SocketServerUDP.clients.remove(player);
            SocketServerUDP.clientByAddress.remove(socketAddress);
        }
    }
}

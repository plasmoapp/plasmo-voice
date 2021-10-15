package su.plo.voice.server.socket;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.server.PlayerManager;
import su.plo.voice.server.VoiceServer;

import java.net.InetAddress;
import java.util.UUID;

@Data
public class SocketClientUDP {
    private final UUID playerUUID;
    private final InetAddress address;
    private final int port;
    private long keepAlive;
    private long sentKeepAlive;
    @Setter(AccessLevel.PROTECTED)
    private String type;

    public SocketClientUDP(UUID player, String type, InetAddress address, int port) {
        this.playerUUID = player;
        this.type = type;
        this.address = address;
        this.port = port;
        this.keepAlive = System.currentTimeMillis();
    }

    public ServerPlayer getPlayer() {
        return PlayerManager.getByUUID(playerUUID);
    }

    public void close() {
        if (SocketServerUDP.clients.containsKey(playerUUID)) {
            if (!VoiceServer.getInstance().getConfig().getBoolean("disable_logs")) {
                ServerPlayer player = this.getPlayer();
                VoiceServer.LOGGER.info("Remove client UDP: " + player.getGameProfile().getName());
            }
            SocketServerUDP.clients.remove(playerUUID);
        }
    }
}

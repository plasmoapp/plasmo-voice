package su.plo.voice.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.common.packets.tcp.ClientDisconnectedPacket;
import su.plo.voice.common.packets.tcp.PacketTCP;
import su.plo.voice.common.packets.tcp.ServerConnectPacket;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketServerUDP;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener {
    public static HashMap<UUID, UUID> playerToken = new HashMap<>();

    public static void reconnectPlayer(Player player) {
        UUID token = UUID.randomUUID();
        playerToken.put(player.getUniqueId(), token);

        Bukkit.getScheduler().runTaskAsynchronously(PlasmoVoice.getInstance(), () -> {
            try {
                byte[] pkt = PacketTCP.write(new ServerConnectPacket(token.toString(),
                        PlasmoVoice.getInstance().config.ip,
                        PlasmoVoice.getInstance().config.port,
                        player.hasPermission("voice.priority")));

                player.sendPluginMessage(PlasmoVoice.getInstance(), "plasmo:voice", pkt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent e) {
        if(e.getChannel().equals("plasmo:voice")) {
            reconnectPlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if(player.isOp() && !SocketServerUDP.started) {
            player.sendMessage(PlasmoVoice.getInstance().getPrefix() +
                    String.format("Voice chat is installed but doesn't work. Check if port %d UDP is open.",
                    PlasmoVoice.getInstance().config.port));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        playerToken.remove(player);
        Bukkit.getScheduler().runTaskAsynchronously(PlasmoVoice.getInstance(), () -> {
            disconnectClient(player);
        });
    }

    public static void disconnectClient(Player player) {
        SocketClientUDP clientUDP = SocketServerUDP.clients.get(player);

        try {
            if(clientUDP != null) {
                PluginChannelListener.sendToClients(new ClientDisconnectedPacket(player.getUniqueId()));
                clientUDP.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

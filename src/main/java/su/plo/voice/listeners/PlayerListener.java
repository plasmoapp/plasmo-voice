package su.plo.voice.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.common.packets.tcp.ClientDisconnectedPacket;
import su.plo.voice.common.packets.tcp.ClientUnmutedPacket;
import su.plo.voice.common.packets.tcp.PacketTCP;
import su.plo.voice.common.packets.tcp.ServerConnectPacket;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketServerUDP;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener {
    public static HashMap<UUID, UUID> playerToken = new HashMap<>();

    public PlayerListener() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(PlasmoVoice.getInstance(), () -> {
            PlasmoVoice.muted.forEach((uuid, muted) -> {
                if(muted.to > 0 && muted.to < System.currentTimeMillis()) {
                    PlasmoVoice.muted.remove(uuid);
                    Player player = Bukkit.getPlayer(uuid);
                    if(player != null) {
                        PluginChannelListener.sendToClients(new ClientUnmutedPacket(muted.uuid), player);
                    }
                }
            });
        }, 0L, 100L);
    }

    public static void reconnectPlayer(Player player) {
        UUID token = UUID.randomUUID();
        playerToken.put(player.getUniqueId(), token);

        Bukkit.getScheduler().runTaskAsynchronously(PlasmoVoice.getInstance(), () -> {
            try {
                byte[] pkt = PacketTCP.write(new ServerConnectPacket(token.toString(),
                        PlasmoVoice.getInstance().config.proxyIp != null
                                ? PlasmoVoice.getInstance().config.proxyIp : PlasmoVoice.getInstance().config.ip,
                        PlasmoVoice.getInstance().config.proxyPort != 0
                                ? PlasmoVoice.getInstance().config.proxyPort : PlasmoVoice.getInstance().config.port,
                        player.hasPermission("voice.priority")));

                player.sendPluginMessage(PlasmoVoice.getInstance(), "plasmo:voice", pkt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent e) {
        if(!SocketServerUDP.started) { // send connect packet only if socket started
            return;
        }

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

        if(PlasmoVoice.getInstance().config.clientModRequired) {
            Bukkit.getScheduler().runTaskLater(PlasmoVoice.getInstance(), () -> {
                if (!SocketServerUDP.clients.containsKey(player)) {
                    PlasmoVoice.logger.info(String.format("Player: %s does not have the mod installed!", player.getName()));
                    player.kick(Component.text(PlasmoVoice.getInstance().getMessage("mod_missing_kick_message")));
                }
            }, PlasmoVoice.getInstance().config.clientModCheckTimeout);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        playerToken.remove(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(PlasmoVoice.getInstance(), () -> {
            disconnectClient(player);
        });
    }

    public static void disconnectClient(Player player) {
        SocketClientUDP clientUDP = SocketServerUDP.clients.get(player);

        try {
            if(clientUDP != null) {
                clientUDP.close();
                PluginChannelListener.sendToClients(new ClientDisconnectedPacket(player.getUniqueId()), player);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

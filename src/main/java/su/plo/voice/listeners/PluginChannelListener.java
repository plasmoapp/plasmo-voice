package su.plo.voice.listeners;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.PlasmoVoiceConfig;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.*;
import su.plo.voice.data.ServerMutedEntity;
import su.plo.voice.socket.SocketServerUDP;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class PluginChannelListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if(!channel.equals("plasmo:voice")) {
            return;
        }

        try {
            Packet packet = PacketTCP.read(ByteStreams.newDataInput(bytes));
            if(packet instanceof ClientConnectPacket) {
                ClientConnectPacket connect = (ClientConnectPacket) packet;
                PlasmoVoiceConfig config = PlasmoVoice.getInstance().config;

                String version = connect.getVersion();
                int ver = PlasmoVoice.calculateVersion(version);

                if(ver > PlasmoVoice.version) {
                    player.spigot().sendMessage(new TranslatableComponent("message.plasmo_voice.version_not_supported", PlasmoVoice.rawVersion));
                    return;
                } else if(ver < PlasmoVoice.minVersion) {
                    player.spigot().sendMessage(new TranslatableComponent("message.plasmo_voice.min_version", PlasmoVoice.rawMinVersion));
                    return;
                } else if(ver < PlasmoVoice.version) {
                    TextComponent link = new TextComponent(PlasmoVoice.downloadLink);
                    link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, PlasmoVoice.downloadLink));

                    player.spigot().sendMessage(new TranslatableComponent("message.plasmo_voice.new_version_available",
                            PlasmoVoice.rawVersion,
                            link));
                }

                byte[] pkt = PacketTCP.write(new ConfigPacket(config.sampleRate,
                        new ArrayList<>(config.distances),
                        config.defaultDistance,
                        config.maxPriorityDistance,
                        config.disableVoiceActivation || !player.hasPermission("voice.activation"),
                        config.fadeDivisor,
                        config.priorityFadeDivisor));
                player.sendPluginMessage(PlasmoVoice.getInstance(), "plasmo:voice", pkt);

                List<UUID> clients = new ArrayList<>();
                SocketServerUDP.clients.forEach((p, c) -> clients.add(p.getUniqueId()));

                List<MutedEntity> muted = new ArrayList<>();
                PlasmoVoice.muted.forEach((uuid, m) -> muted.add(new MutedEntity(m.uuid, m.to)));

                ServerMutedEntity serverPlayerMuted = PlasmoVoice.muted.get(player.getUniqueId());
                MutedEntity playerMuted = null;
                if (serverPlayerMuted != null) {
                    playerMuted = new MutedEntity(serverPlayerMuted.uuid, serverPlayerMuted.to);
                }
                if(!player.hasPermission("voice.speak")) {
                    playerMuted = new MutedEntity(player.getUniqueId(), 0L);
                }

                pkt = PacketTCP.write(new ClientsListPacket(clients, muted));
                player.sendPluginMessage(PlasmoVoice.getInstance(), "plasmo:voice", pkt);

                sendToClients(new ClientConnectedPacket(player.getUniqueId(), playerMuted), player.getUniqueId());

                PlasmoVoice.logger.info(String.format("New client: %s v%s", player.getName(), version));
            }
        } catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void sendToClients(Packet packet) {
        Bukkit.getScheduler().runTaskAsynchronously(PlasmoVoice.getInstance(), () -> {
            try {
                byte[] pkt = PacketTCP.write(packet);
                Enumeration<Player> it = SocketServerUDP.clients.keys();
                while (it.hasMoreElements()) {
                    Player player = it.nextElement();
                    player.sendPluginMessage(PlasmoVoice.getInstance(), "plasmo:voice", pkt);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendToClients(Packet packet, UUID except) {
        Bukkit.getScheduler().runTaskAsynchronously(PlasmoVoice.getInstance(), () -> {
            try {
                byte[] pkt = PacketTCP.write(packet);
                Enumeration<Player> it = SocketServerUDP.clients.keys();
                while (it.hasMoreElements()) {
                    Player player = it.nextElement();
                    if(!player.getUniqueId().equals(except)) {
                        player.sendPluginMessage(PlasmoVoice.getInstance(), "plasmo:voice", pkt);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

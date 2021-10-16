package su.plo.voice.socket;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.common.packets.udp.*;
import su.plo.voice.events.PlayerEndSpeakEvent;
import su.plo.voice.events.PlayerStartSpeakEvent;
import su.plo.voice.listeners.PlayerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SocketServerUDPQueue extends Thread {
    public LinkedBlockingQueue<PacketUDP> queue = new LinkedBlockingQueue<>();

    public void run() {
        while (!this.isInterrupted()) {
            try {
                this.keepAlive();

                PacketUDP message = queue.poll(10, TimeUnit.MILLISECONDS);
                if (message == null || message.getPacket() == null || System.currentTimeMillis() - message.getTimestamp() > message.getTTL()) {
                    continue;
                }

                if (message.getPacket() instanceof AuthPacket) {
                    AuthPacket packet = (AuthPacket) message.getPacket();
                    AtomicReference<Player> player = new AtomicReference<>();

                    PlayerListener.playerToken.forEach((p, t) -> {
                        if (t.toString().equals(packet.getToken())) {
                            player.set(Bukkit.getPlayer(p));
                        }
                    });

                    if (player.get() != null) {
                        String type = player.get().getListeningPluginChannels().contains("fml:handshake") ? "forge" : "fabric";
                        SocketClientUDP sock = new SocketClientUDP(player.get(), type, message.getAddress(), message.getPort());

                        if (!SocketServerUDP.clients.containsKey(player.get())) {
                            SocketServerUDP.clients.put(player.get(), sock);
                        }

                        try {
                            SocketServerUDP.sendTo(PacketUDP.write(new AuthPacketAck()), sock);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                SocketClientUDP client = SocketServerUDP.getSender(message);
                if (client == null) { // not authorized
                    continue;
                }
                Player player = client.getPlayer();
                if (player == null) {
                    continue;
                }

                if (message.getPacket() instanceof PingPacket) {
                    client.setKeepAlive(System.currentTimeMillis());
                    continue;
                }

                // server mute
                if (PlasmoVoice.getInstance().isMuted(player.getUniqueId())) {
                    continue;
                }

                if (message.getPacket() instanceof VoiceClientPacket) {
                    VoiceClientPacket packet = (VoiceClientPacket) message.getPacket();
                    if (!player.hasPermission("voice.speak")) {
                        continue;
                    }

                    if (packet.getDistance() > PlasmoVoice.getInstance().getVoiceConfig().getMaxDistance()) {
                        if (player.hasPermission("voice.priority") &&
                                packet.getDistance() <= PlasmoVoice.getInstance().getVoiceConfig().getMaxPriorityDistance()) {
                            VoiceServerPacket serverPacket = new VoiceServerPacket(packet.getData(),
                                    player.getUniqueId(),
                                    packet.getSequenceNumber(),
                                    packet.getDistance()
                            );
                            SocketServerUDP.sendToNearbyPlayers(serverPacket, player, packet.getDistance());
                        } else {
                            continue;
                        }
                    } else {
                        VoiceServerPacket serverPacket = new VoiceServerPacket(packet.getData(),
                                player.getUniqueId(),
                                packet.getSequenceNumber(),
                                packet.getDistance()
                        );
                        SocketServerUDP.sendToNearbyPlayers(serverPacket, player, packet.getDistance());
                    }

                    if (!SocketServerUDP.talking.containsKey(player.getUniqueId())) {
                        SocketServerUDP.talking.put(player.getUniqueId(), System.currentTimeMillis());
                        Bukkit.getPluginManager().callEvent(new PlayerStartSpeakEvent(player));
                    } else {
                        SocketServerUDP.talking.put(player.getUniqueId(), System.currentTimeMillis());
                    }
                } else if (message.getPacket() instanceof VoiceEndClientPacket) {
                    VoiceEndClientPacket packet = (VoiceEndClientPacket) message.getPacket();

                    VoiceEndServerPacket serverPacket = new VoiceEndServerPacket(player.getUniqueId());
                    SocketServerUDP.sendToNearbyPlayers(serverPacket, player, packet.getDistance());

                    if (SocketServerUDP.talking.containsKey(player.getUniqueId())) {
                        SocketServerUDP.talking.remove(player.getUniqueId());
                        Bukkit.getPluginManager().callEvent(new PlayerEndSpeakEvent(player));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException ignored) {
                break;
            }
        }
    }

    private void keepAlive() throws IOException {
        long timestamp = System.currentTimeMillis();
        PingPacket keepAlive = new PingPacket();
        List<Player> connectionsToDrop = new ArrayList<>(SocketServerUDP.clients.size());
        for (SocketClientUDP connection : SocketServerUDP.clients.values()) {
            if (SocketServerUDP.talking.containsKey(connection.getPlayer().getUniqueId())) {
                if (timestamp - SocketServerUDP.talking.get(connection.getPlayer().getUniqueId()) > 250L) {
                    SocketServerUDP.talking.remove(connection.getPlayer().getUniqueId());
                    Bukkit.getPluginManager().callEvent(new PlayerEndSpeakEvent(connection.getPlayer()));
                }
            }

            if (timestamp - connection.getKeepAlive() >= 15000L) {
                connectionsToDrop.add(connection.getPlayer());
            } else if (timestamp - connection.getSentKeepAlive() >= 1000L) {
                connection.setSentKeepAlive(timestamp);
                SocketServerUDP.sendTo(PacketUDP.write(keepAlive), connection);
            }
        }
        for (Player player : connectionsToDrop) {
            PlayerListener.disconnectClient(player);

            if (!PlasmoVoice.getInstance().getConfig().getBoolean("disable_logs")) {
                PlasmoVoice.getVoiceLogger().info(player.getName() + " UDP timed out");
                PlasmoVoice.getVoiceLogger().info(player.getName() + " sent reconnect packet");
            }

            PlayerListener.reconnectPlayer(player);
        }
    }
}

package su.plo.voice.socket;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.plo.voice.PlasmoVoice;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.tcp.ClientUnmutedPacket;
import su.plo.voice.common.packets.udp.*;
import su.plo.voice.listeners.PlayerListener;
import su.plo.voice.listeners.PluginChannelListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SocketServerUDPQueue extends Thread {
    public LinkedBlockingQueue<PacketUDP> queue = new LinkedBlockingQueue<>();
    public boolean running = true;

    public void run() {
        while (running) {
            try {
                this.keepAlive();

                PacketUDP message = queue.poll(10, TimeUnit.MILLISECONDS);
                if (message == null || message.getPacket() == null || System.currentTimeMillis() - message.getTimestamp() > message.getTTL()) {
                    continue;
                }

                if(message.getPacket() instanceof AuthPacket) {
                    AuthPacket packet = (AuthPacket) message.getPacket();
                    AtomicReference<Player> player = new AtomicReference<>();

                    PlayerListener.playerToken.forEach((p, t) -> {
                        if(t.toString().equals(packet.getToken())) {
                            player.set(Bukkit.getPlayer(p));
                        }
                    });

                    if(player.get() != null) {
                        SocketClientUDP sock = new SocketClientUDP(player.get(), message.getAddress(), message.getPort());

                        if(!SocketServerUDP.clients.containsKey(player.get())) {
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

                // server mute
                if(PlasmoVoice.muted.containsKey(player.getUniqueId())) {
                    MutedEntity e = PlasmoVoice.muted.get(player.getUniqueId());
                    if(e == null) {
                        continue;
                    }

                    if(e.to == 0 || e.to > System.currentTimeMillis()) {
                        continue;
                    } else {
                        PlasmoVoice.muted.remove(e.uuid);

                        Bukkit.getScheduler().runTask(PlasmoVoice.getInstance(), () -> {
                            PluginChannelListener.sendToClients(new ClientUnmutedPacket(e.uuid));
                        });
                    }
                }

                if(message.getPacket() instanceof VoiceClientPacket) {
                    VoiceClientPacket packet = (VoiceClientPacket) message.getPacket();
                    if(!player.hasPermission("voice.speak")) {
                        continue;
                    }

                    if(packet.getDistance() > PlasmoVoice.getInstance().config.maxDistance) {
                        if(player.hasPermission("voice.priority") && packet.getDistance() <= PlasmoVoice.getInstance().config.maxPriorityDistance) {
                            VoiceServerPacket serverPacket = new VoiceServerPacket(packet.getData(),
                                    player.getUniqueId(),
                                    packet.getSequenceNumber(),
                                    packet.getDistance()
                            );
                            SocketServerUDP.sendToNearbyPlayers(serverPacket, player, packet.getDistance());
                        }
                    } else {
                        VoiceServerPacket serverPacket = new VoiceServerPacket(packet.getData(),
                                player.getUniqueId(),
                                packet.getSequenceNumber(),
                                packet.getDistance()
                        );
                        SocketServerUDP.sendToNearbyPlayers(serverPacket, player, packet.getDistance());
                    }

                } else if(message.getPacket() instanceof VoiceEndClientPacket) {
                    VoiceEndClientPacket packet = (VoiceEndClientPacket) message.getPacket();

                    VoiceEndServerPacket serverPacket = new VoiceEndServerPacket(player.getUniqueId());
                    SocketServerUDP.sendToNearbyPlayers(serverPacket, player, packet.getDistance());
                } else if(message.getPacket() instanceof PingPacket) {
                    client.setKeepAlive(System.currentTimeMillis());
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void keepAlive() throws IOException {
        long timestamp = System.currentTimeMillis();
        PingPacket keepAlive = new PingPacket();
        List<Player> connectionsToDrop = new ArrayList<>(SocketServerUDP.clients.size());
        for (SocketClientUDP connection : SocketServerUDP.clients.values()) {
            if (timestamp - connection.getKeepAlive() >= 1000 * 30L) {
                connectionsToDrop.add(connection.getPlayer());
            } else if (timestamp - connection.getKeepAlive() >= 1000) {
                connection.setKeepAlive(timestamp);
                SocketServerUDP.sendTo(PacketUDP.write(keepAlive), connection);
            }
        }
        for (Player player : connectionsToDrop) {
            PlayerListener.disconnectClient(player);

            PlasmoVoice.logger.info(player.getName() + " UDP timed out");

            PlasmoVoice.logger.info(player.getName() + " sended reconnect packet");
            PlayerListener.reconnectPlayer(player);
        }
    }

    public void close() {
        this.running = false;
    }
}

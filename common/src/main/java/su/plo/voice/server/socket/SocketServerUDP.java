package su.plo.voice.server.socket;

import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.udp.PacketUDP;
import su.plo.voice.server.PlayerManager;
import su.plo.voice.server.VoiceServer;

import java.io.IOException;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SocketServerUDP extends Thread {
    public static final ConcurrentHashMap<UUID, SocketClientUDP> clients = new ConcurrentHashMap<>();
    private final SocketAddress addr;

    public static boolean started;
    private final SocketServerUDPQueue queue;
    private static DatagramSocket socket;

    public SocketServerUDP(String ip, int port) {
        this.addr = new InetSocketAddress(ip, port);
        this.queue = new SocketServerUDPQueue();
        this.queue.start();
    }

    public static void sendToNearbyPlayers(Packet packet, ServerPlayer player, double maxDistance) {
        double maxDistanceSquared = maxDistance * maxDistance * 1.25F;

        byte[] bytes;
        try {
            bytes = PacketUDP.write(packet);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        SocketServerUDP.clients.forEach((uuid, sock) -> {
            if (!player.getUUID().equals(uuid)) {
                ServerPlayer p = PlayerManager.getByUUID(uuid);

                if (maxDistanceSquared > 0) {
                    if (!player.getLevel().equals(p.getLevel())) {
                        return;
                    }

                    try {
                        if (player.position().distanceToSqr(p.position()) > maxDistanceSquared) {
                            return;
                        }
                    } catch (IllegalArgumentException ignored) {
                        return;
                    }
                }

                try {
                    SocketServerUDP.sendTo(bytes, sock);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static SocketClientUDP getSender(PacketUDP packet) {
        return clients.values().stream()
                .filter(connection -> connection.getAddress()
                        .equals(packet.getAddress()) && connection.getPort() == packet.getPort())
                .findAny().orElse(null);
    }

    public static void sendTo(byte[] data, SocketClientUDP connection) throws IOException {
        socket.send(new DatagramPacket(data, data.length, connection.getAddress(), connection.getPort()));
    }

    public void close() {
        if (socket != null) {
            socket.close();
            queue.interrupt();
        }
        this.interrupt();
    }

    public void run() {
        try {
            socket = new DatagramSocket(this.addr);
            socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
        } catch (SocketException e) {
            e.printStackTrace();
            VoiceServer.LOGGER.info(String.format("Failed to bind socket. Check if port %d UDP is open",
                    VoiceServer.getServerConfig().getPort()));
            return;
        }

        started = true;
        VoiceServer.LOGGER.info("Voice UDP server started on {}", addr.toString());

        while (!socket.isClosed()) {
            try {
                PacketUDP message = PacketUDP.read(socket);
                this.queue.queue.offer(message);

                synchronized (this.queue) {
                    this.queue.notify();
                }
            } catch (IllegalStateException | IOException | InstantiationException | IllegalArgumentException  e) { // bad packet? just ignore it 4HEad
                if (VoiceServer.getInstance().getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        VoiceServer.LOGGER.info("Voice UDP server stopped");
    }
}

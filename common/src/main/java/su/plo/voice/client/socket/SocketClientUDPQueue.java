package su.plo.voice.client.socket;

import net.minecraft.client.Minecraft;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.client.sound.openal.OpenALPlayerQueue;
import su.plo.voice.common.packets.udp.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketClientUDPQueue extends Thread {
    private static final Minecraft client = Minecraft.getInstance();
    private final SocketClientUDP socket;
    public ConcurrentLinkedQueue<PacketUDP> queue = new ConcurrentLinkedQueue<>();
    public static final Map<UUID, AbstractSoundQueue> audioChannels = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<UUID, Boolean> talking = new ConcurrentHashMap<>();

    public SocketClientUDPQueue(SocketClientUDP socket) {
        this.socket = socket;
    }

    public static void closeAll() {
        // kill all queues to prevent possible problems
        audioChannels.values()
                .forEach(AbstractSoundQueue::closeAndKill);
        audioChannels.clear();
        talking.clear();
    }

    private void queuePacket(VoiceServerPacket packet, UUID uuid) {
        AbstractSoundQueue ch = audioChannels.get(uuid);
        if (ch == null || ch.isClosed()) {
            VoiceClient.getServerConfig().getClients().add(uuid);
            if (packet instanceof VoiceServerPacket) {
                ch = new OpenALPlayerQueue(uuid);
                ch.addQueue(packet);
            }

            audioChannels.put(uuid, ch);
        } else {
            ch.addQueue(packet);
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            if (!this.queue.isEmpty()) {
                PacketUDP message = this.queue.poll();

                if (message.getPacket() instanceof AuthPacketAck) {
                    if (!socket.authorized) {
                        VoiceClient.LOGGER.info("Connected to UDP");
                        socket.authorized = true;

                        if (client.screen instanceof VoiceNotAvailableScreen) {
                            client.execute(() ->
                                client.setScreen(new VoiceSettingsScreen())
                            );
                        }
                    }
                } else if (message.getPacket() instanceof VoiceServerPacket packet) {
                    if (VoiceClient.getClientConfig().isMuted(packet.getFrom())) {
                        continue;
                    }

                    if (VoiceClient.getClientConfig().speakerMuted.get()) {
                        continue;
                    }

                    talking.put(packet.getFrom(), packet.getDistance() > VoiceClient.getServerConfig().getMaxDistance());

                    queuePacket(packet, packet.getFrom());
                } else if (message.getPacket() instanceof VoiceEndServerPacket packet) {
                    if (VoiceClient.getClientConfig().isMuted(packet.getFrom())) {
                        continue;
                    }

                    talking.remove(packet.getFrom());

                    AbstractSoundQueue ch = audioChannels.get(packet.getFrom());
                    if (ch != null) {
                        ch.addQueue(new VoiceServerPacket(new byte[0], packet.getFrom(), ch.lastSequenceNumber + 1,
                                VoiceClient.isSpeakingPriority()
                                        ? VoiceClient.getServerConfig().getPriorityDistance()
                                        : VoiceClient.getServerConfig().getDistance()));
                    }
                } else if (message.getPacket() instanceof PingPacket) {
                    this.socket.keepAlive = System.currentTimeMillis();
                    this.socket.ping.timedOut = false;
                    try {
                        this.socket.send(new PingPacket());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
        }
    }
}

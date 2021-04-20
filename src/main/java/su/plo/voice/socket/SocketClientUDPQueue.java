package su.plo.voice.socket;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.common.packets.udp.*;
import su.plo.voice.sound.ThreadSoundQueue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketClientUDPQueue extends Thread {
    private final SocketClientUDP socket;
    public ConcurrentLinkedQueue<PacketUDP> queue = new ConcurrentLinkedQueue<>();
    public static final Map<UUID, ThreadSoundQueue> audioChannels = new HashMap<>();

    public static final ConcurrentHashMap<UUID, Boolean> talking = new ConcurrentHashMap<>();

    public SocketClientUDPQueue(SocketClientUDP socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while(!socket.isClosed()) {
            if(!this.queue.isEmpty()) {
                PacketUDP message = this.queue.poll();

                if(message.getPacket() instanceof AuthPacketAck) {
                    if(!socket.authorized) {
                        VoiceClient.LOGGER.info("Connected to UDP");
                        socket.authorized = true;
                    }
                } else if(message.getPacket() instanceof VoiceServerPacket) {
                    VoiceServerPacket voicePacket = (VoiceServerPacket) message.getPacket();

                    if(VoiceClient.clientMutedClients.contains(voicePacket.getFrom())) {
                        continue;
                    }

                    talking.put(voicePacket.getFrom(), voicePacket.getDistance() > VoiceClient.serverConfig.maxDistance);

                    ThreadSoundQueue ch = audioChannels.get(voicePacket.getFrom());
                    if(ch == null) {
                        ch = new ThreadSoundQueue(voicePacket.getFrom());
                        ch.addQueue(voicePacket);
                        ch.start();

                        audioChannels.put(voicePacket.getFrom(), ch);
                    } else {
                        ch.addQueue(voicePacket);
                    }
                } else if(message.getPacket() instanceof VoiceEndServerPacket) {
                    VoiceEndServerPacket voicePacket = (VoiceEndServerPacket) message.getPacket();

                    if(VoiceClient.clientMutedClients.contains(voicePacket.getFrom())) {
                        continue;
                    }

                    talking.remove(voicePacket.getFrom());

                    ThreadSoundQueue ch = audioChannels.get(voicePacket.getFrom());
                    if(ch != null) {
                        ch.addQueue(new VoiceServerPacket(new byte[0], voicePacket.getFrom(), ch.lastSequenceNumber + 1,
                                VoiceClient.speakingPriority ? VoiceClient.serverConfig.priorityDistance : VoiceClient.serverConfig.distance));
                    }
                } else if(message.getPacket() instanceof PingPacket) {
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
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }
}

package su.plo.voice.client;

import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.tcp.ConfigPacket;
import su.plo.voice.socket.SocketClientUDPQueue;
import su.plo.voice.sound.ThreadSoundQueue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceServerConfig {
    public String secret;
    public String ip;
    public int port;
    public HashSet<UUID> clients = new HashSet<>();
    public ConcurrentHashMap<UUID, MutedEntity> mutedClients = new ConcurrentHashMap<>();
    public List<Integer> distances = new ArrayList<>();
    public short distance;
    public short minDistance;
    public short maxDistance;
    public short maxPriorityDistance;
    public short priorityDistance;
    public int fadeDivisor;
    public int priorityFadeDivisor;
    public boolean hasPriority;
    public boolean disableVoiceActivation;

    public VoiceServerConfig(String secret, String ip, int port, boolean hasPriority) {
        this.secret = secret;
        this.ip = ip;
        this.port = port;
        this.hasPriority = hasPriority;
    }

    public void update(ConfigPacket config) {
        this.distances = config.getDistances();
        Collections.sort(this.distances);
        this.minDistance = this.distances.get(0).shortValue();
        this.maxDistance = this.distances.get(this.distances.size() - 1).shortValue();
        this.maxPriorityDistance = (short) config.getMaxPriorityDistance();
        this.maxPriorityDistance = this.maxPriorityDistance == 0 ? Short.MAX_VALUE : this.maxPriorityDistance;
        this.fadeDivisor = config.getFadeDivisor();
        this.priorityFadeDivisor = config.getPriorityFadeDivisor();
        this.disableVoiceActivation = config.isDisableVoiceActivation();

        if(VoiceClient.config.servers.containsKey(ip)) {
            VoiceClientServerConfig serverConfig = VoiceClient.config.servers.get(ip);
            if(this.distances.contains((int) serverConfig.distance)) {
                this.distance = serverConfig.distance;
            } else {
                this.distance = (short) config.getDefaultDistance();
            }

            if(serverConfig.priorityDistance > this.maxDistance && serverConfig.priorityDistance < this.maxPriorityDistance) {
                this.priorityDistance = serverConfig.priorityDistance;
            } else {
                this.priorityDistance = (short) Math.min(this.maxPriorityDistance, this.maxDistance * 2);
            }
        } else {
            this.distance = (short) config.getDefaultDistance();
            this.priorityDistance = (short) Math.min(this.maxPriorityDistance, this.maxDistance * 2);
        }

        VoiceClient.speaking = false;
        VoiceClient.speakingPriority = false;

        VoiceClient.recorder.updateConfig(config.getSampleRate());

        SocketClientUDPQueue.talking.clear();
        SocketClientUDPQueue.audioChannels.values().forEach(ThreadSoundQueue::closeAndKill);
        SocketClientUDPQueue.audioChannels.clear();
    }
}

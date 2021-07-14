package su.plo.voice.client;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.tcp.ConfigPacket;
import su.plo.voice.socket.SocketClientUDPQueue;
import su.plo.voice.sound.ThreadSoundQueue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class VoiceServerConfig {
    private String secret;
    private String ip;
    private int port;
    @Setter(AccessLevel.PRIVATE)
    private HashSet<UUID> clients = new HashSet<>();
    @Setter(AccessLevel.PRIVATE)
    private ConcurrentHashMap<UUID, MutedEntity> muted = new ConcurrentHashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private List<Integer> distances = new ArrayList<>();
    private short distance;
    private short minDistance;
    private short maxDistance;
    private short maxPriorityDistance;
    private short priorityDistance;
    private int fadeDivisor;
    private int priorityFadeDivisor;
    private boolean priority;
    private boolean voiceActivationDisabled;

    public VoiceServerConfig(String secret, String ip, int port, boolean hasPriority) {
        this.secret = secret;
        this.ip = ip;
        this.port = port;
        this.priority = hasPriority;
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
        this.voiceActivationDisabled = config.isDisableVoiceActivation();

        if(VoiceClient.getClientConfig().getServers().containsKey(ip)) {
            VoiceClientServerConfig serverConfig = VoiceClient.getClientConfig().getServers().get(ip);
            if(this.distances.contains((int) serverConfig.getDistance())) {
                this.distance = serverConfig.getDistance();
            } else {
                this.distance = (short) config.getDefaultDistance();
            }

            if(serverConfig.getPriorityDistance() > this.maxDistance &&
                    serverConfig.getPriorityDistance() < this.maxPriorityDistance) {
                this.priorityDistance = serverConfig.getPriorityDistance();
            } else {
                this.priorityDistance = (short) Math.min(this.maxPriorityDistance, this.maxDistance * 2);
            }
        } else {
            this.distance = (short) config.getDefaultDistance();
            this.priorityDistance = (short) Math.min(this.maxPriorityDistance, this.maxDistance * 2);
        }

        VoiceClient.setSpeaking(false);
        VoiceClient.setSpeakingPriority(false);

        VoiceClient.recorder.updateConfig(config.getSampleRate());

        SocketClientUDPQueue.talking.clear();
        SocketClientUDPQueue.audioChannels.values().forEach(ThreadSoundQueue::closeAndKill);
        SocketClientUDPQueue.audioChannels.clear();
    }
}

package su.plo.voice.server.config;

import lombok.Data;

import java.util.HashSet;
import java.util.List;

@Data
public class ServerConfig {
    private final String ip;
    private final int port;
    private final String proxyIp;
    private final int proxyPort;
    private final int sampleRate;
    private final HashSet<Integer> distances = new HashSet<>();
    private final int maxDistance;
    private final int defaultDistance;
    private final short maxPriorityDistance;
    private final int fadeDivisor;
    private final int priorityFadeDivisor;
    private final boolean disableVoiceActivation;

    public ServerConfig(String ip, int port,
                        String proxyIp, int proxyPort,
                        int sampleRate, List<Integer> distances, int defaultDistance,
                        int maxPriorityDistance, boolean disableVoiceActivation,
                        int fadeDivisor, int priorityFadeDivisor) {
        this.ip = ip;
        this.port = port;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        this.sampleRate = sampleRate;
        this.distances.addAll(distances);
        this.maxDistance = distances.get(distances.size() - 1);
        this.defaultDistance = defaultDistance;
        this.maxPriorityDistance = maxPriorityDistance > 0 ? (short) maxPriorityDistance : Short.MAX_VALUE;
        this.fadeDivisor = fadeDivisor;
        this.priorityFadeDivisor = priorityFadeDivisor;
        this.disableVoiceActivation = disableVoiceActivation;
    }
}
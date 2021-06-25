package su.plo.voice;

import java.util.HashSet;
import java.util.List;

public class PlasmoVoiceConfig {
    public final String ip;
    public final int port;
    public final String proxyIp;
    public final int proxyPort;
    public final int sampleRate;
    public final HashSet<Integer> distances = new HashSet<>();
    public final int maxDistance;
    public final int defaultDistance;
    public final short maxPriorityDistance;
    public final int fadeDivisor;
    public final int priorityFadeDivisor;
    public final boolean disableVoiceActivation;
    public final boolean clientModRequired;
    public final int clientModCheckTimeout;

    public PlasmoVoiceConfig(String ip, int port,
                             String proxyIp, int proxyPort,
                             int sampleRate, List<Integer> distances, int defaultDistance,
                             int maxPriorityDistance, boolean disableVoiceActivation,
                             int fadeDivisor, int priorityFadeDivisor, boolean clientModRequired, int clientModCheckTimeout) {
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
        this.clientModRequired = clientModRequired;
        this.clientModCheckTimeout = clientModCheckTimeout;
    }
}

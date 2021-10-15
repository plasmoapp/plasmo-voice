package su.plo.voice.server.config;

import lombok.Getter;

import java.util.ArrayList;

public class ServerConfigFabric extends ServerConfig {
    @Getter
    private final boolean clientModRequired;
    @Getter
    private final int clientModCheckTimeout;

    public ServerConfigFabric(ServerConfig config, boolean clientModRequired, int clientModCheckTimeout) {
        super(config.getIp(), config.getPort(), config.getProxyIp(), config.getProxyPort(), config.getSampleRate(),
                new ArrayList<>(config.getDistances()), config.getDefaultDistance(), config.getMaxPriorityDistance(),
                config.isDisableVoiceActivation(), config.getFadeDivisor(), config.getPriorityFadeDivisor());
        this.clientModRequired = clientModRequired;
        this.clientModCheckTimeout = clientModCheckTimeout;
    }
}

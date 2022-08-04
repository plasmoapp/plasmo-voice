package su.plo.voice.client;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.connection.UdpClientManager;
import su.plo.voice.api.client.event.VoiceClientInitializedEvent;
import su.plo.voice.api.client.event.VoiceClientShutdownEvent;
import su.plo.voice.client.audio.device.VoiceDeviceFactoryManager;
import su.plo.voice.client.audio.device.VoiceDeviceManager;
import su.plo.voice.client.connection.VoiceUdpClientManager;

import java.util.Optional;

public abstract class BaseVoiceClient extends BaseVoice implements PlasmoVoiceClient {

    public static final String CHANNEL_STRING = "plasmo:voice";

    protected final Logger logger = LogManager.getLogger("PlasmoVoiceClient");

    @Getter
    private final DeviceFactoryManager deviceFactoryManager = new VoiceDeviceFactoryManager();
    @Getter
    private final DeviceManager deviceManager = new VoiceDeviceManager();
    @Getter
    private final UdpClientManager udpClientManager = new VoiceUdpClientManager();

    @Setter
    private ServerInfo currentServerInfo;

    protected void onInitialize() {

        getEventBus().call(new VoiceClientInitializedEvent(this));
    }

    protected void onShutdown() {
        logger.info("Shutting down");

        getEventBus().call(new VoiceClientShutdownEvent(this));
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public Optional<ServerInfo> getCurrentServerInfo() {
        return Optional.ofNullable(currentServerInfo);
    }

    public abstract String getServerIp();
}

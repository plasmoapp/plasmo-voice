package su.plo.voice.client;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.connection.UdpClientManager;
import su.plo.voice.api.client.event.VoiceClientInitializedEvent;
import su.plo.voice.api.client.event.VoiceClientShutdownEvent;
import su.plo.voice.client.audio.device.VoiceDeviceFactoryManager;
import su.plo.voice.client.audio.device.VoiceDeviceManager;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.connection.VoiceUdpClientManager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public abstract class BaseVoiceClient extends BaseVoice implements PlasmoVoiceClient {

    public static final String CHANNEL_STRING = "plasmo:voice";

    protected static final ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    protected final Logger logger = LogManager.getLogger("PlasmoVoiceClient");

    @Getter
    private final DeviceFactoryManager deviceFactoryManager = new VoiceDeviceFactoryManager();
    @Getter
    private final DeviceManager deviceManager = new VoiceDeviceManager();
    @Getter
    private final UdpClientManager udpClientManager = new VoiceUdpClientManager();

    @Setter
    private ServerInfo currentServerInfo;

    protected ClientConfig config;

    protected void onInitialize() {
        try {
            this.config = toml.load(ClientConfig.class, new File(configFolder(), "client.toml"), true);
            System.out.println(config);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config", e);
        }

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

    @Override
    public @NotNull KeyBindings getKeyBindings() {
        return config.getKeyBindings();
    }

    public abstract String getServerIp();
}

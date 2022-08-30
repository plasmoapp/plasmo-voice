package su.plo.voice.server;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ActivationManager;
import su.plo.voice.api.server.audio.source.ServerSourceManager;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.event.VoiceServerInitializeEvent;
import su.plo.voice.api.server.event.VoiceServerShutdownEvent;
import su.plo.voice.api.server.event.socket.UdpServerCreateEvent;
import su.plo.voice.api.server.event.socket.UdpServerStartedEvent;
import su.plo.voice.api.server.event.socket.UdpServerStoppedEvent;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.server.audio.capture.VoiceActivationManager;
import su.plo.voice.server.audio.source.VoiceServerSourceManager;
import su.plo.voice.server.config.ServerConfig;
import su.plo.voice.server.connection.VoiceTcpConnectionManager;
import su.plo.voice.server.connection.VoiceUdpConnectionManager;
import su.plo.voice.server.socket.NettyUdpServer;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public abstract class BaseVoiceServer extends BaseVoice implements PlasmoVoiceServer {

    public static final String CHANNEL_STRING = "plasmo:voice";

    protected static final ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    protected final Logger logger = LogManager.getLogger();
    protected final TcpServerConnectionManager tcpConnections = new VoiceTcpConnectionManager(this);
    protected final UdpServerConnectionManager udpConnections = new VoiceUdpConnectionManager(this);
    protected final ServerSourceManager sources = new VoiceServerSourceManager(this);

    protected UdpServer udpServer;
    protected ActivationManager activations;

    @Getter
    protected ServerConfig config;

    @Override
    protected void onInitialize() {
        eventBus.call(new VoiceServerInitializeEvent(this));
        eventBus.register(this, sources);

        try {
            this.config = toml.load(ServerConfig.class, new File(configFolder(), "config.toml"), true);
            System.out.println(config);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config", e);
        }

        this.activations = new VoiceActivationManager(config.getVoice());

        UdpServer server = new NettyUdpServer(this);

        UdpServerCreateEvent createEvent = new UdpServerCreateEvent(server);
        eventBus.call(createEvent);
        if (createEvent.isCancelled()) return;

        server = createEvent.getServer();

        try {
            int port = config.getHost().getPort();
            if (port == 0) {
                port = getMinecraftServerPort();
                if (port <= 0) port = 60606;
            }

            server.start(config.getHost().getIp(), port);
            eventBus.call(new UdpServerStartedEvent(server));
            this.udpServer = server;
        } catch (Exception e) {
            getLogger().error("Failed to start the udp server", e);
            return;
        }
    }

    @Override
    protected void onShutdown() {
        eventBus.call(new VoiceServerShutdownEvent(this));

        if (this.udpServer != null) {
            udpServer.stop();
            udpConnections.clearConnections();
            eventBus.call(new UdpServerStoppedEvent(udpServer));
        }

        eventBus.unregister(this);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public @NotNull ServerSourceManager getSourceManager() {
        return sources;
    }

    @Override
    public @NotNull ActivationManager getActivationManager() {
        return activations;
    }

    @Override
    public @NotNull TcpServerConnectionManager getTcpConnectionManager() {
        return tcpConnections;
    }

    @Override
    public @NotNull UdpServerConnectionManager getUdpConnectionManager() {
        return udpConnections;
    }

    @Override
    public Optional<UdpServer> getUdpServer() {
        return Optional.ofNullable(udpServer);
    }

    public abstract int getMinecraftServerPort();
}

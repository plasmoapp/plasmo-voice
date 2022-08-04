package su.plo.voice.server;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfigBuilder;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.event.VoiceServerInitializeEvent;
import su.plo.voice.api.server.event.VoiceServerShutdownEvent;
import su.plo.voice.api.server.event.socket.UdpServerCreateEvent;
import su.plo.voice.api.server.event.socket.UdpServerStartedEvent;
import su.plo.voice.api.server.event.socket.UdpServerStoppedEvent;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.config.ConfigLoader;
import su.plo.voice.server.config.ServerConfig;
import su.plo.voice.server.connection.VoiceTcpConnectionManager;
import su.plo.voice.server.connection.VoiceUdpConnectionManager;
import su.plo.voice.server.socket.NettyUdpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public abstract class BaseVoiceServer extends BaseVoice implements PlasmoVoiceServer {

    public static final String CHANNEL_STRING = "plasmo:voice";

    protected final Logger logger = LogManager.getLogger("PlasmoVoiceServer");
    protected final TcpServerConnectionManager tcpConnections = new VoiceTcpConnectionManager(this);
    protected final UdpServerConnectionManager udpConnections = new VoiceUdpConnectionManager(this);

    protected UdpServer udpServer;

    @Getter
    protected ServerConfig config;

    @Override
    protected void onInitialize() {
        eventBus.call(new VoiceServerInitializeEvent(this));

        ConfigLoader loader = new ConfigLoader(
                new File(configFolder(), "config.toml"),
                getResource("server.toml")
        );
        try {
            this.config = loader.getConfig(ServerConfig.class);
            System.out.println(config);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config:", e);
        }

        UdpServer server = new NettyUdpServer(
                eventBus,
                getTcpConnectionManager(),
                getUdpConnectionManager(),
                getPlayerManager()
        );

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
            eventBus.call(new UdpServerStoppedEvent(udpServer));
        }
    }

    @Override
    protected Logger getLogger() {
        return logger;
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

    private void reloadConfig() throws IOException {
        try (InputStream is = getResource("config.toml")) {
            CommentedFileConfigBuilder builder = CommentedFileConfig.builder(
                    new File(configFolder(), "config.toml")
            );
            builder.onFileNotFound(FileNotFoundAction.copyData(is));
        }
    }
}

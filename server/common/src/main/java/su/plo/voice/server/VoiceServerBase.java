package su.plo.voice.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.VoiceBase;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.connection.ConnectionManager;
import su.plo.voice.api.server.event.VoiceServerInitializeEvent;
import su.plo.voice.api.server.event.VoiceServerShutdownEvent;
import su.plo.voice.api.server.event.socket.UdpServerCreateEvent;
import su.plo.voice.api.server.event.socket.UdpServerStartedEvent;
import su.plo.voice.api.server.event.socket.UdpServerStoppedEvent;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.server.connection.VoiceConnectionManager;
import su.plo.voice.server.socket.NettyUdpSocket;

import java.util.Optional;

public abstract class VoiceServerBase extends VoiceBase implements PlasmoVoiceServer {

    protected final Logger logger = LogManager.getLogger("PlasmoVoiceServer");
    protected final ConnectionManager connections = new VoiceConnectionManager(this);

    protected UdpServer udpServer;

    @Override
    protected void onInitialize() {
        eventBus.call(new VoiceServerInitializeEvent(this));

        // todo: load config

        UdpServer server = new NettyUdpSocket(eventBus, getConnectionManager(), getPlayerManager());

        UdpServerCreateEvent createEvent = new UdpServerCreateEvent(server);
        eventBus.call(createEvent);
        if (createEvent.isCancelled()) return;

        server = createEvent.getServer();

        try {
            server.start("127.0.0.1", 60606); // todo: use config
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
    public @NotNull ConnectionManager getConnectionManager() {
        return connections;
    }

    @Override
    public Optional<UdpServer> getUdpServer() {
        return Optional.ofNullable(udpServer);
    }
}

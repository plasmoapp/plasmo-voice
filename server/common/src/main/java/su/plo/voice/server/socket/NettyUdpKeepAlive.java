package su.plo.voice.server.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.server.config.VoiceServerConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class NettyUdpKeepAlive {

    private final Logger logger = LogManager.getLogger(NettyUdpKeepAlive.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final TcpServerConnectionManager tcpConnections;
    private final UdpServerConnectionManager udpConnections;
    private final VoiceServerConfig config;

    public NettyUdpKeepAlive(@NotNull TcpServerConnectionManager tcpConnections,
                             @NotNull UdpServerConnectionManager udpConnections,
                             @NotNull VoiceServerConfig config) {
        this.tcpConnections = tcpConnections;
        this.udpConnections = udpConnections;
        this.config = config;

        executor.scheduleAtFixedRate(this::tick, 0L, 3L, TimeUnit.SECONDS);
    }

    public void close() {
        executor.shutdown();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        PingPacket packet = new PingPacket();

        for (UdpServerConnection connection : udpConnections.getConnections()) {
            if (now - connection.getKeepAlive() > config.voice().keepAliveTimeoutMs()) {
                logger.info("{} timed out. Reconnect packet sent", connection);
                udpConnections.removeConnection(connection);
                tcpConnections.connect(connection.getPlayer());
            } else if (now - connection.getSentKeepAlive() >= 1_000L) {
                connection.setSentKeepAlive(now);
                connection.sendPacket(packet);
            }
        }
    }
}

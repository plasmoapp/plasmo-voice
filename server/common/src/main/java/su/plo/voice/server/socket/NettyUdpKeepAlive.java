package su.plo.voice.server.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.server.BaseVoiceServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class NettyUdpKeepAlive {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final BaseVoiceServer voiceServer;

    public NettyUdpKeepAlive(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        executor.scheduleAtFixedRate(this::tick, 0L, 3L, TimeUnit.SECONDS);
    }

    public void close() {
        executor.shutdown();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        PingPacket packet = new PingPacket();

        for (UdpServerConnection connection : voiceServer.getUdpConnectionManager().getConnections()) {
            if (now - connection.getKeepAlive() > voiceServer.getConfig().voice().keepAliveTimeoutMs()) {
                voiceServer.getDebugLogger().log("UDP connection timed out: {}", connection);
                voiceServer.getUdpConnectionManager().removeConnection(connection);
                voiceServer.getTcpConnectionManager().requestPlayerInfo(connection.getPlayer());
            } else if (now - connection.getSentKeepAlive() >= 1_000L) {
                connection.setSentKeepAlive(now);
                connection.sendPacket(packet);
            }
        }
    }
}

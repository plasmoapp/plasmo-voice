package su.plo.voice.server.socket;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.metrics.Metrics;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class NettyUdpKeepAlive {

    private final static long KEEP_ALIVE_INTERVAL = 2_500L;

    private final BaseVoiceServer voiceServer;

    private final Random jitterRandom = new Random();

    private ScheduledFuture<?> tickFuture;

    public NettyUdpKeepAlive(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;
    }

    public void start(@NotNull Channel channel) {
        this.tickFuture = channel.eventLoop().scheduleAtFixedRate(
                this::tick,
                0L,
                100L,
                TimeUnit.MILLISECONDS
        );
    }

    public void close() {
        if (tickFuture != null) tickFuture.cancel(true);
    }

    private void tick() {
        long now = System.currentTimeMillis();
        PingPacket packet = new PingPacket();

        Metrics metrics = voiceServer.getMetrics();

        for (UdpServerConnection connection : voiceServer.getUdpConnectionManager().getConnections()) {
            if (now - connection.getLastReceivedPacketTimestamp() > voiceServer.getConfig().voice().keepAliveTimeoutMs()) {
                BaseVoice.DEBUG_LOGGER.log("UDP connection timed out: {}", connection);
                voiceServer.getUdpConnectionManager().removeConnection(connection, UdpClientDisconnectedEvent.Reason.TIMED_OUT);
                voiceServer.getTcpPacketManager().requestPlayerInfo(connection.getPlayer());
            } else if (now >= connection.getNextKeepAlive()) {
                long jitter = jitterRandom.nextInt(1_500);
                connection.setNextKeepAlive(now + KEEP_ALIVE_INTERVAL + jitter);
                connection.setSentKeepAlive(now);

                connection.sendPacket(packet);
            }

            if (metrics != null) {
                long lag = now - connection.getLastReceivedPacketTimestamp();
                metrics.recordKeepAliveTimeSeconds(Duration.ofMillis(lag));
            }
        }
    }
}

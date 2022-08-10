package su.plo.voice.client.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.socket.NettyPacketUdp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NettyUdpClientKeepAlive extends SimpleChannelInboundHandler<NettyPacketUdp> {

    private final Logger logger = LogManager.getLogger(NettyUdpClientKeepAlive.class);

    private final NettyUdpClient client;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private long keepAlive = System.currentTimeMillis();

    public NettyUdpClientKeepAlive(@NotNull NettyUdpClient client) {
        executor.scheduleAtFixedRate(this::tick, 0L, 1L, TimeUnit.SECONDS);
        this.client = checkNotNull(client, "client");
    }

    public void close() {
        executor.shutdown();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyPacketUdp packetUdp) throws Exception {
        Packet<?> packet = packetUdp.getPacketUdp().getPacket();
        logger.info("{} received", packet);
        if (packet instanceof PingPacket) {
            client.setTimedOut(false);
            this.keepAlive = System.currentTimeMillis();

            client.sendPacket(new PingPacket());
        }
    }

    private void tick() {
        if (!client.getRemoteAddress().isPresent()) return;

        if (!client.isConnected()) {
            this.keepAlive = System.currentTimeMillis();
            client.sendPacket(new PingPacket());
            return;
        }

        // todo: config for max timeout keepalive?
        long diff = System.currentTimeMillis() - keepAlive;
        if (diff > 30_000L) {
            logger.warn("UDP timed out. Disconnecting...");
            client.close(UdpClientClosedEvent.Reason.TIMED_OUT);
        } else if (diff > 7_000L) {
            client.setTimedOut(true);
        }
    }
}

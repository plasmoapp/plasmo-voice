package su.plo.voice.client.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.event.connection.ServerInfoInitializedEvent;
import su.plo.voice.api.client.event.connection.UdpClientPacketSendEvent;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectedEvent;
import su.plo.voice.api.client.event.socket.UdpClientTimedOutEvent;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.socket.NettyPacketUdpDecoder;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NettyUdpClient implements UdpClient {

    private static final Logger LOGGER = LogManager.getLogger();

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;
    @Getter
    private final UUID secret;

    private final EventLoopGroup workGroup = new NioEventLoopGroup();
    private final NettyUdpClientHandler handler;
    private NioDatagramChannel channel;

    @Getter
    private boolean closed;
    @Getter
    private boolean connected;
    @Getter
    private boolean timedOut;

    public NettyUdpClient(@NotNull BaseVoiceClient voiceClient,
                          @NotNull ClientConfig config,
                          @NotNull UUID secret) {
        this.voiceClient = checkNotNull(voiceClient, "voiceClient");
        this.config = checkNotNull(config, "config");
        this.secret = checkNotNull(secret, "secret");
        this.handler = new NettyUdpClientHandler(voiceClient, config, this);
    }

    @Override
    public void connect(String ip, int port) {
        if (isClosed()) throw new IllegalStateException("Client is closed and cannot be reused");

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup);
        bootstrap.channel(NioDatagramChannel.class);
        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            protected void initChannel(@NotNull NioDatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("decoder", new NettyPacketUdpDecoder());

                pipeline.addLast("handler", handler);
            }
        });

        try {
            LOGGER.info("Connecting to {}:{}", ip, port);
            ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
            this.channel = (NioDatagramChannel) channelFuture.channel();
        } catch (InterruptedException e) {
            close(UdpClientClosedEvent.Reason.FAILED_TO_CONNECT);
        } catch (Exception e) {
            close(UdpClientClosedEvent.Reason.FAILED_TO_CONNECT);
            throw e;
        }
    }

    @Override
    public void close(@NotNull UdpClientClosedEvent.Reason reason) {
        if (channel == null) {
            LOGGER.info("Disconnecting before connecting with reason {}", reason);
        } else {
            LOGGER.info("Disconnecting from {} with reason {}", channel.remoteAddress(), reason);
        }

        handler.close();
        workGroup.shutdownGracefully();
        this.closed = true;
        this.connected = false;

        voiceClient.getEventBus().unregister(voiceClient, this);

        voiceClient.getEventBus().call(new UdpClientClosedEvent(this, reason));
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        byte[] encoded = PacketUdpCodec.encode(packet, secret);
        if (encoded == null) return;

        ByteBuf buf = Unpooled.wrappedBuffer(encoded);

        LOGGER.debug("UDP packet {} sent to {}", packet, channel.remoteAddress());

        UdpClientPacketSendEvent event = new UdpClientPacketSendEvent(this, packet);
        if (!voiceClient.getEventBus().call(event)) return;

        channel.writeAndFlush(new DatagramPacket(buf, channel.remoteAddress()));
    }

    @Override
    public Optional<InetSocketAddress> getRemoteAddress() {
        return channel != null
                ? Optional.ofNullable(channel.remoteAddress())
                : Optional.empty();
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
        voiceClient.getEventBus().call(new UdpClientTimedOutEvent(this, timedOut));
    }

    @EventSubscribe
    public void onServerInfoUpdate(ServerInfoInitializedEvent event) {
        if (this.connected) return;

        LOGGER.info("Connected to {}", channel.remoteAddress());
        this.connected = true;

        voiceClient.getEventBus().call(new UdpClientConnectedEvent(this));
    }
}

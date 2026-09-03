package su.plo.voice.proxy.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketDirection;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;
import su.plo.voice.proxy.connection.CancelForwardingException;
import su.plo.voice.socket.NettyExceptionHandler;
import su.plo.voice.socket.NettyPacketUdp;
import su.plo.voice.socket.NettyPacketUdpDecoder;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public final class NettyUdpProxyConnection implements UdpProxyConnection, ServerPacketUdpHandler {

    private final PlasmoVoiceProxy voiceProxy;
    private final DatagramChannel channel;

    @Getter
    private final VoiceProxyPlayer player;
    @Getter
    private final UUID secret;

    private final EventLoopGroup loopGroup;
    private final Class<? extends DatagramChannel> channelClass;

    @Getter @Setter
    private volatile UUID remoteSecret;
    @Getter @Setter
    private volatile InetSocketAddress connectionAddress;
    @Getter @Setter
    private volatile InetSocketAddress remoteAddress;

    private RemoteServer remoteServer;
    private final AtomicReference<ChannelFuture> remoteChannelFuture = new AtomicReference<>();
    private volatile boolean connected = true;

    @Override
    public Optional<RemoteServer> getRemoteServer() {
        return Optional.ofNullable(remoteServer);
    }

    @Override
    public void setRemoteServer(@NotNull RemoteServer remoteServer) {
        RemoteServer previousServer = this.remoteServer;
        this.remoteServer = remoteServer;

        InetSocketAddress address = remoteServer.getAddress(true);
        if (previousServer != null && address.equals(previousServer.getAddress())) return;

        connectToRemoteServer(address);
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        if (!isConnected() || player.getInstance().getServer() == null) return;

        byte[] encoded = PacketUdpCodec.encode(packet, secret);
        if (encoded == null) return;

        ByteBuf buf = Unpooled.wrappedBuffer(encoded);

        channel.writeAndFlush(new DatagramPacket(buf, remoteAddress));
    }

    @Override
    public void handlePacket(Packet<ServerPacketUdpHandler> packet) {
        packet.handle(this);
    }

    @Override
    public void disconnect() {
        this.connected = false;
        closeChannel(remoteChannelFuture.getAndSet(null));
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void handle(@NotNull PingPacket packet) {
    }

    @Override
    public void handle(@NotNull CustomPacket packet) {
    }

    @Override
    public void handle(@NotNull PlayerAudioPacket packet) {
        if (!voiceProxy.getEventBus().fire(new PlayerSpeakEvent(player, packet))) {
            throw new CancelForwardingException();
        }
    }

    void sendPacketToRemoteServer(@NotNull NettyPacketUdp nettyPacket) {
        ChannelFuture channelFuture = remoteChannelFuture.get();
        if (channelFuture == null || !connected) return;

        UUID remoteSecret = this.remoteSecret;
        if (remoteSecret == null) return;

        if (!channelFuture.isSuccess()) {
            deferPingPacket(nettyPacket, channelFuture, remoteSecret);
            return;
        }

        ByteBuf buf = Unpooled.wrappedBuffer(
                PacketUdpCodec.replaceSecret(nettyPacket.getPacketData(), remoteSecret)
        );

        channelFuture.channel().writeAndFlush(buf);
    }

    private void connectToRemoteServer(@NotNull InetSocketAddress address) {
        if (!connected) return;

        BaseVoice.DEBUG_LOGGER.log(
                "Connecting to remote server {} for {}",
                address, player.getInstance().getName()
        );

        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(loopGroup)
                .channel(channelClass);

        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(@NotNull DatagramChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("decoder", new NettyPacketUdpDecoder(PacketDirection.CLIENT));
                pipeline.addLast("handler", new RemoteServerPacketHandler());
                pipeline.addLast("exception_handler", new NettyExceptionHandler());
            }
        });

        ChannelFuture channelFuture = bootstrap.connect(address);
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) return;
            BaseVoice.DEBUG_LOGGER.log("Failed to connect to remote server {}", address, future.cause());
        });

        closeChannel(remoteChannelFuture.getAndSet(channelFuture));

        if (!connected && remoteChannelFuture.compareAndSet(channelFuture, null)) {
            channelFuture.channel().close();
        }
    }

    private void deferPingPacket(
            @NotNull NettyPacketUdp nettyPacket,
            @NotNull ChannelFuture channelFuture,
            @NotNull UUID remoteSecret
    ) {
        if (!nettyPacket.getPacketUdp().getPacketClass().equals(PingPacket.class)) return;

        ByteBuf buf = Unpooled.wrappedBuffer(
                PacketUdpCodec.replaceSecret(nettyPacket.getPacketData(), remoteSecret)
        );

        channelFuture.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess() || !connected) {
                buf.release();
                return;
            }
            future.channel().writeAndFlush(buf);
        });
    }

    private void closeChannel(@Nullable ChannelFuture channelFuture) {
        if (channelFuture != null) channelFuture.channel().close();
    }

    private final class RemoteServerPacketHandler extends SimpleChannelInboundHandler<NettyPacketUdp> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, NettyPacketUdp nettyPacket) {
            if (!connected || player.getInstance().getServer() == null) return;

            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.wrappedBuffer(PacketUdpCodec.replaceSecret(nettyPacket.getPacketData(), secret)),
                    remoteAddress
            ));
        }
    }
}

package su.plo.voice.proxy.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.event.socket.UdpProxyServerStoppedEvent;
import su.plo.voice.api.proxy.socket.UdpProxyServer;
import su.plo.voice.proto.packets.PacketDirection;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.socket.NettyExceptionHandler;
import su.plo.voice.socket.NettyPacketUdpDecoder;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public final class NettyUdpProxyServer implements UdpProxyServer {

    private final boolean useEpoll = System.getProperty("plasmovoice.use_epoll", "true").equals("true") &&
            Epoll.isAvailable();

    private final BaseVoiceProxy voiceProxy;

    private final EventLoopGroup loopGroup;

    private DatagramChannel channel;
    private InetSocketAddress socketAddress;

    public NettyUdpProxyServer(@NotNull BaseVoiceProxy voiceServer) {
        this.voiceProxy = voiceServer;

        this.loopGroup = useEpoll
                ? new EpollEventLoopGroup()
                : new NioEventLoopGroup();
    }

    @Override
    public void start(String ip, int port) {
        InetAddress bindAddress;
        try {
            bindAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            BaseVoice.LOGGER.error("Failed to resolve UDP proxy server bind host {}:{}", ip, port);
            throw new RuntimeException(e);
        }
        InternetProtocolFamily bindAddressFamily = bindAddress instanceof Inet6Address
                ? InternetProtocolFamily.IPv6
                : InternetProtocolFamily.IPv4;

        Class<? extends DatagramChannel> channelClass = useEpoll
                ? EpollDatagramChannel.class
                : NioDatagramChannel.class;

        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(loopGroup)
                .channelFactory(() -> useEpoll
                        ? new EpollDatagramChannel(bindAddressFamily)
                        : new NioDatagramChannel(bindAddressFamily)
                );

        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(@NotNull DatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("decoder", new NettyPacketUdpDecoder(PacketDirection.ANY));
                pipeline.addLast("handler", new NettyPacketHandler(voiceProxy));
                pipeline.addLast("exception_handler", new NettyExceptionHandler());
            }
        });

        BaseVoice.LOGGER.info("UDP proxy server is starting on {}:{} (family: {})", ip, port, bindAddressFamily);
        try {
            ChannelFuture channelFuture = bootstrap.bind(bindAddress, port).sync();
            this.channel = (DatagramChannel) channelFuture.channel();
            this.socketAddress = channel.localAddress();
        } catch (InterruptedException e) {
            BaseVoice.LOGGER.warn(
                    "Interrupted while starting the {} UDP proxy server on {}:{} (family: {})",
                    channelClass.getSimpleName(), ip, port, bindAddressFamily
            );
            stop();
            return;
        } catch (Exception e) {
            BaseVoice.LOGGER.error(
                    "Failed to start the {} UDP proxy server on {}:{} (family: {})",
                    channelClass.getSimpleName(), ip, port, bindAddressFamily
            );
            stop();
            throw e;
        }
        BaseVoice.LOGGER.info("{} UDP proxy server is started on {}", channelClass.getSimpleName(), socketAddress);
    }

    @Override
    public void stop() {
        if (channel != null) channel.close();
        loopGroup.shutdownGracefully();
        BaseVoice.LOGGER.info("UDP proxy server is stopped");

        voiceProxy.getEventBus().fire(new UdpProxyServerStoppedEvent(this));
    }

    @Override
    public Optional<InetSocketAddress> getRemoteAddress() {
        return Optional.ofNullable(socketAddress);
    }
}

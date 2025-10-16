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
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.event.socket.UdpProxyServerStoppedEvent;
import su.plo.voice.api.proxy.socket.UdpProxyServer;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.socket.NettyExceptionHandler;
import su.plo.voice.socket.NettyPacketUdpDecoder;

import java.net.InetSocketAddress;
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

        Class<? extends DatagramChannel> channelClass = useEpoll
                ? EpollDatagramChannel.class
                : NioDatagramChannel.class;

        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(loopGroup)
                .channel(channelClass);

        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(@NotNull DatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("decoder", new NettyPacketUdpDecoder());
                pipeline.addLast("handler", new NettyPacketHandler(voiceProxy));
                pipeline.addLast("exception_handler", new NettyExceptionHandler());
            }
        });

        BaseVoice.LOGGER.info("UDP proxy server is starting on {}:{}", ip, port);
        try {
            ChannelFuture channelFuture = bootstrap.bind(ip, port).sync();
            this.channel = (DatagramChannel) channelFuture.channel();
            this.socketAddress = channel.localAddress();
        } catch (InterruptedException e) {
            stop();
            return;
        } catch (Exception e) {
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

package su.plo.voice.proxy.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.event.socket.UdpProxyServerStoppedEvent;
import su.plo.voice.api.proxy.socket.UdpProxyServer;
import su.plo.voice.proto.packets.PacketDirection;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.socket.NettyExceptionHandler;
import su.plo.voice.socket.NettyPacketUdpDecoder;
import su.plo.voice.util.SystemPropertyKt;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

public final class NettyUdpProxyServer implements UdpProxyServer {

    private static final int DEFAULT_THREADS = Math.max(
            1,
            SystemPropertyKt.getIntSystemProperty(
                    "plasmovoice.udp_threads",
                    Runtime.getRuntime().availableProcessors() * 2
            )
    );

    private final boolean useEpoll = System.getProperty("plasmovoice.use_epoll", "true").equals("true") &&
            Epoll.isAvailable();

    private final BaseVoiceProxy voiceProxy;

    private final EventLoopGroup loopGroup;
    private final Class<? extends DatagramChannel> channelClass;

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private InetSocketAddress socketAddress;

    public NettyUdpProxyServer(@NotNull BaseVoiceProxy voiceServer) {
        this.voiceProxy = voiceServer;

        ThreadFactory factory = new DefaultThreadFactory("plasmo-voice-udp", Thread.MAX_PRIORITY);

        this.loopGroup = useEpoll
                ? new EpollEventLoopGroup(DEFAULT_THREADS, factory)
                : new NioEventLoopGroup(DEFAULT_THREADS, factory);
        this.channelClass = useEpoll
                ? EpollDatagramChannel.class
                : NioDatagramChannel.class;
    }

    @Override
    public void start(String ip, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(loopGroup)
                .channel(channelClass);

        boolean reusePortEnabled = false;
        if (useEpoll && voiceProxy.getConfig().reusePort().enabled()) {
            try {
                bootstrap.option(UnixChannelOption.SO_REUSEPORT, true);
                reusePortEnabled = true;
            } catch (Exception e) {
                BaseVoice.LOGGER.warn("SO_REUSEPORT not supported on this platform, falling back to single channel");
            }
        }

        int socketsAmount = reusePortEnabled
                ? voiceProxy.getConfig().reusePort().sockets()
                : 1;
        if (socketsAmount <= 0) {
            socketsAmount = Runtime.getRuntime().availableProcessors() * 2;
        }

        boolean pinConnectionToChannel = reusePortEnabled && socketsAmount > 1;

        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(@NotNull DatagramChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("flush_consolidation", new FlushConsolidationHandler(256, true));
                pipeline.addLast("decoder", new NettyPacketUdpDecoder(PacketDirection.SERVER));
                pipeline.addLast("handler", new NettyPacketHandler(voiceProxy, loopGroup, channelClass, pinConnectionToChannel));
                pipeline.addLast("exception_handler", new NettyExceptionHandler());
            }
        });

        BaseVoice.LOGGER.info("UDP proxy server is starting on {}:{}", ip, port);
        try {
            for (int i = 0; i < socketsAmount; i++) {
                ChannelFuture channelFuture = bootstrap.bind(ip, port).sync();
                Channel channel = channelFuture.channel();
                channelGroup.add(channel);

                this.socketAddress = (InetSocketAddress) channel.localAddress();
            }

            if (reusePortEnabled && socketsAmount > 1) {
                BaseVoice.LOGGER.info(
                        "Bound {} {} UDP proxy instances with SO_REUSEPORT on {}",
                        socketsAmount,
                        channelClass.getSimpleName(),
                        socketAddress
                );
            } else {
                BaseVoice.LOGGER.info("{} UDP proxy is started on {}", channelClass.getSimpleName(), socketAddress);
            }
        } catch (InterruptedException e) {
            stop();
        } catch (Exception e) {
            stop();
            throw e;
        }
    }

    @Override
    public void stop() {
        voiceProxy.getUdpConnectionManager().clearConnections();
        channelGroup.close();
        loopGroup.shutdownGracefully();
        BaseVoice.LOGGER.info("UDP proxy server is stopped");

        voiceProxy.getEventBus().fire(new UdpProxyServerStoppedEvent(this));
    }

    @Override
    public Optional<InetSocketAddress> getRemoteAddress() {
        return Optional.ofNullable(socketAddress);
    }
}

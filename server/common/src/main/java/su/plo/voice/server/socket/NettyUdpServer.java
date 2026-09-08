package su.plo.voice.server.socket;

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
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.proto.packets.PacketDirection;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.socket.NettyExceptionHandler;
import su.plo.voice.socket.NettyPacketUdpDecoder;
import su.plo.voice.util.SystemPropertyKt;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

public final class NettyUdpServer implements UdpServer {

    // Spigot overwrites io.netty.eventLoopThreads with spigot.yml's netty-threads (default 4)
    // so we have to avoid netty default amount of threads path
    private static final int DEFAULT_THREADS = Math.max(
            1,
            SystemPropertyKt.getIntSystemProperty(
                    "plasmovoice.udp_threads",
                    Runtime.getRuntime().availableProcessors() * 2
            )
    );

    private final boolean useEpoll = System.getProperty("plasmovoice.use_epoll", "true").equals("true") &&
            Epoll.isAvailable();

    private final EventLoopGroup loopGroup;
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final BaseVoiceServer voiceServer;

    private NettyUdpKeepAlive keepAlive;

    private InetSocketAddress socketAddress;

    public NettyUdpServer(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        ThreadFactory factory = new DefaultThreadFactory("plasmo-voice-udp", Thread.MAX_PRIORITY);

        this.loopGroup = useEpoll
                ? new EpollEventLoopGroup(DEFAULT_THREADS, factory)
                : new NioEventLoopGroup(DEFAULT_THREADS, factory);
    }

    @Override
    public void start(String ip, int port) {
        this.keepAlive = new NettyUdpKeepAlive(voiceServer);

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

                pipeline.addLast("flush_consolidation", new FlushConsolidationHandler(256, true));
                pipeline.addLast("decoder", new NettyPacketUdpDecoder(PacketDirection.SERVER));
                pipeline.addLast("handler", new NettyPacketHandler(voiceServer));
                pipeline.addLast("exception_handler", new NettyExceptionHandler());
            }
        });

        boolean reusePortEnabled = false;
        if (useEpoll && voiceServer.getConfig().voice().reusePort().enabled()) {
            try {
                bootstrap.option(UnixChannelOption.SO_REUSEPORT, true);
                reusePortEnabled = true;
            } catch (Exception e) {
                BaseVoice.LOGGER.warn("SO_REUSEPORT not supported on this platform, falling back to single channel");
            }
        }

        try {
            Channel firstChannel = null;
            int socketsAmount = reusePortEnabled
                    ? voiceServer.getConfig().voice().reusePort().sockets()
                    : 1;
            if (socketsAmount <= 0) {
                socketsAmount = Runtime.getRuntime().availableProcessors() * 2;
            }

            for (int i = 0; i < socketsAmount; i++) {
                ChannelFuture channelFuture = bootstrap.bind(ip, port).sync();
                Channel channel = channelFuture.channel();
                channelGroup.add(channel);

                if (firstChannel == null) {
                    firstChannel = channel;
                    this.socketAddress = (InetSocketAddress) channel.localAddress();
                }
            }

            if (firstChannel != null) {
                keepAlive.start(firstChannel);
            }

            if (reusePortEnabled && socketsAmount > 1) {
                BaseVoice.LOGGER.info(
                        "Bound {} {} UDP server instances with SO_REUSEPORT on {}",
                        socketsAmount,
                        channelClass.getSimpleName(),
                        socketAddress
                );
            } else {
                BaseVoice.LOGGER.info("{} UDP server is started on {}", channelClass.getSimpleName(), socketAddress);
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
        voiceServer.getUdpConnectionManager().clearConnections();
        if (keepAlive != null) keepAlive.close();
        channelGroup.close();
        loopGroup.shutdownGracefully();

        BaseVoice.LOGGER.info("UDP server is stopped");
    }

    @Override
    public Optional<InetSocketAddress> getRemoteAddress() {
        return Optional.ofNullable(socketAddress);
    }
}

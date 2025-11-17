package su.plo.voice.server.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.SystemPropertyUtil;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.metrics.Metrics;
import su.plo.voice.server.metrics.NettyPacketExceptionHandlerMetrics;
import su.plo.voice.server.metrics.NettyPacketHandlerPostDecoderMetrics;
import su.plo.voice.server.metrics.NettyPacketHandlerPreDecoderMetrics;
import su.plo.voice.server.metrics.NettyPacketHandlerPreHandlerMetrics;
import su.plo.voice.socket.NettyExceptionHandler;
import su.plo.voice.socket.NettyPacketUdpDecoder;

import java.net.InetSocketAddress;
import java.util.Optional;

public final class NettyUdpServer implements UdpServer {

    private final boolean useEpoll = System.getProperty("plasmovoice.use_epoll", "true").equals("true") &&
            Epoll.isAvailable();

    private final EventLoopGroup loopGroup;
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final BaseVoiceServer voiceServer;

    private NettyUdpKeepAlive keepAlive;

    private InetSocketAddress socketAddress;

    public NettyUdpServer(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        this.loopGroup = useEpoll
                ? new EpollEventLoopGroup()
                : new NioEventLoopGroup();
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

                pipeline.addLast("decoder", new NettyPacketUdpDecoder());
                pipeline.addLast("decoder_exception_handler", new NettyExceptionHandler("Failed to decode packet"));

                pipeline.addLast("handler", new NettyPacketHandler(voiceServer));
                pipeline.addLast("handler_exception_handler", new NettyExceptionHandler());

                Metrics metrics = voiceServer.getMetrics();
                if (metrics != null) {
                    metrics.attachNettyAllocatorMetrics(pipeline.channel().alloc());
                    metrics.attachNettyExecutorMetrics(loopGroup);

                    pipeline.addBefore("handler", "metrics_pre_handler", new NettyPacketHandlerPreHandlerMetrics(voiceServer));

                    pipeline.addBefore("decoder", "metrics_pre_decoder", new NettyPacketHandlerPreDecoderMetrics(voiceServer));
                    pipeline.addAfter("decoder", "metrics_post_decoder", new NettyPacketHandlerPostDecoderMetrics(voiceServer));

                    pipeline.replace(
                            "decoder_exception_handler",
                            "decoder_exception_handler",
                            new NettyPacketExceptionHandlerMetrics(
                                    voiceServer,
                                    Metrics.PacketHandlerErrorStage.Decode,
                                    (ChannelInboundHandler) pipeline.get("decoder_exception_handler")
                            )
                    );
                    pipeline.replace(
                            "handler_exception_handler",
                            "handler_exception_handler",
                            new NettyPacketExceptionHandlerMetrics(
                                    voiceServer,
                                    Metrics.PacketHandlerErrorStage.Handle,
                                    (ChannelInboundHandler) pipeline.get("handler_exception_handler")
                            )
                    );
                }
            }
        });

        boolean reusePortEnabled = false;
        if (useEpoll && voiceServer.getConfig().voice().reusePort()) {
            try {
                bootstrap.option(UnixChannelOption.SO_REUSEPORT, true);
                reusePortEnabled = true;
            } catch (Exception e) {
                BaseVoice.LOGGER.warn("SO_REUSEPORT not supported on this platform, falling back to single channel");
            }
        }

        try {
            Channel firstChannel = null;
            int channelCount = reusePortEnabled
                    ? ((MultithreadEventLoopGroup) loopGroup).executorCount()
                    : 1;

            for (int i = 0; i < channelCount; i++) {
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

            if (reusePortEnabled && channelCount > 1) {
                BaseVoice.LOGGER.info(
                        "Bound {} {} UDP server instances with SO_REUSEPORT on {}",
                        channelCount,
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

package su.plo.voice.server.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.socket.NettyPacketUdpDecoder;

@RequiredArgsConstructor
public final class NettyUdpServer implements UdpServer {

    private final Logger logger = LogManager.getLogger(NettyUdpServer.class);

    private final EventLoopGroup loopGroup = new NioEventLoopGroup();
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final EventExecutorGroup executors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors());

    private final PlasmoVoiceServer voiceServer;
    private NettyUdpKeepAlive keepAlive;

    @Override
    public void start(String ip, int port) {
        this.keepAlive = new NettyUdpKeepAlive(voiceServer.getTcpConnectionManager(), voiceServer.getUdpConnectionManager());

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_BROADCAST, true);

        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(@NotNull NioDatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("decoder", new NettyPacketUdpDecoder());

                pipeline.addLast(executors, "handler", new NettyPacketHandler(voiceServer));
            }
        });

        logger.info("UDP server is starting on {}:{}", ip, port);
        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelGroup.add(channelFuture.channel());
        } catch (InterruptedException e) {
            stop();
            return;
        } catch (Exception e) {
            stop();
            throw e;
        }
        logger.info("UDP server is started on {}:{}", ip, port);
    }

    @Override
    public void stop() {
        if (keepAlive != null) keepAlive.close();
        channelGroup.close();
        loopGroup.shutdownGracefully();
        logger.info("UDP server is stopped");
    }
}

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
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.api.server.connection.ConnectionManager;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.socket.UdpServer;

@AllArgsConstructor
public class NettyUdpSocket implements UdpServer {

    private final Logger logger = LogManager.getLogger(NettyUdpSocket.class);

    private final EventLoopGroup loopGroup = new NioEventLoopGroup();
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);;
    private final EventExecutorGroup executors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors());

    private final EventBus eventBus;
    private final ConnectionManager connections;
    private final PlayerManager players;

    @Override
    public void start(String ip, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_BROADCAST, true);

        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(@NotNull NioDatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("decoder", new NettyMessageDecoder());

                pipeline.addLast(executors, "handler", new NettyHandshakeHandler(eventBus, connections, players));
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
        loopGroup.shutdownGracefully();
        logger.info("UDP server is stopped");
    }
}

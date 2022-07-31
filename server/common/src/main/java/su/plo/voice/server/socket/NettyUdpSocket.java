package su.plo.voice.server.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.connection.ConnectionManager;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.socket.UdpServer;

@AllArgsConstructor
public class NettyUdpSocket implements UdpServer {

    private final EventLoopGroup loopGroup = new NioEventLoopGroup();
    private final EventExecutorGroup executors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors());

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

                pipeline.addLast(executors, "handler", new NettyHandshakeHandler(connections, players));
            }
        });

    }

    @Override
    public void close() {

    }
}

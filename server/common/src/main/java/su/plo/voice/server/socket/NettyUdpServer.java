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
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.socket.NettyPacketUdpDecoder;

import java.net.InetSocketAddress;
import java.util.Optional;

@RequiredArgsConstructor
public final class NettyUdpServer implements UdpServer {

    private final EventLoopGroup loopGroup = new NioEventLoopGroup();
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final EventExecutorGroup executors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors());

    private final BaseVoiceServer voiceServer;

    private NettyUdpKeepAlive keepAlive;

    private InetSocketAddress socketAddress;

    @Override
    public void start(String ip, int port) {
        this.keepAlive = new NettyUdpKeepAlive(voiceServer);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(loopGroup)
                .channel(NioDatagramChannel.class);

        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(@NotNull NioDatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("decoder", new NettyPacketUdpDecoder());

                pipeline.addLast(executors, "handler", new NettyPacketHandler(voiceServer));
            }
        });

        try {
            ChannelFuture channelFuture = bootstrap.bind(ip, port).sync();
            channelGroup.add(channelFuture.channel());
            this.socketAddress = (InetSocketAddress) channelFuture.channel().localAddress();
        } catch (InterruptedException e) {
            stop();
            return;
        } catch (Exception e) {
            stop();
            throw e;
        }

        BaseVoice.LOGGER.info("UDP server is started on {}", socketAddress);
    }

    @Override
    public void stop() {
        voiceServer.getUdpConnectionManager().clearConnections();
        if (keepAlive != null) keepAlive.close();
        channelGroup.close();
        loopGroup.shutdownGracefully();
        executors.shutdownGracefully();

        BaseVoice.LOGGER.info("UDP server is stopped");
    }

    @Override
    public Optional<InetSocketAddress> getRemoteAddress() {
        return Optional.ofNullable(socketAddress);
    }
}

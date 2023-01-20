package su.plo.voice.proxy.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.socket.UdpProxyServer;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.socket.NettyPacketUdpDecoder;

import java.net.InetSocketAddress;
import java.util.Optional;

@RequiredArgsConstructor
public final class NettyUdpProxyServer implements UdpProxyServer {

    private final Logger logger = LogManager.getLogger();

    private final BaseVoiceProxy voiceProxy;

    private final EventLoopGroup loopGroup = new NioEventLoopGroup();
//    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final EventExecutorGroup executors = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors());

    private NioDatagramChannel channel;
    private InetSocketAddress socketAddress;

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

                pipeline.addLast("decoder", new NettyPacketUdpDecoder());
                pipeline.addLast(executors, "handler", new NettyPacketHandler(voiceProxy));
            }
        });

        logger.info("UDP proxy server is starting on {}:{}", ip, port);
        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            this.channel = (NioDatagramChannel) channelFuture.channel();
            this.socketAddress = channel.localAddress();
        } catch (InterruptedException e) {
            stop();
            return;
        } catch (Exception e) {
            stop();
            throw e;
        }
        logger.info("UDP proxy server is started on {}", socketAddress);
    }

    @Override
    public void stop() {
//        voiceServer.getUdpConnectionManager().clearConnections();
//        if (keepAlive != null) keepAlive.close();
//        channelGroup.close();
        channel.close();
        loopGroup.shutdownGracefully();
        logger.info("UDP proxy server is stopped");

//        voiceServer.getEventBus().call(new UdpServerStoppedEvent(this));
    }

    @Override
    public Optional<InetSocketAddress> getRemoteAddress() {
        return Optional.ofNullable(socketAddress);
    }
}

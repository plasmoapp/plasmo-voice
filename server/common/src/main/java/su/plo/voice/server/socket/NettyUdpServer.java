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
import io.netty.util.concurrent.GlobalEventExecutor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.server.BaseVoiceServer;
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
                pipeline.addLast("handler", new NettyPacketHandler(voiceServer));
                pipeline.addLast("exception_handler", new NettyExceptionHandler());
            }
        });

        try {
            ChannelFuture channelFuture = bootstrap.bind(ip, port).sync();
            Channel channel = channelFuture.channel();
            channelGroup.add(channel);
            keepAlive.start(channel);
            this.socketAddress = (InetSocketAddress) channelFuture.channel().localAddress();
        } catch (InterruptedException e) {
            stop();
            return;
        } catch (Exception e) {
            stop();
            throw e;
        }

        BaseVoice.LOGGER.info("{} UDP server is started on {}", channelClass.getSimpleName(), socketAddress);
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

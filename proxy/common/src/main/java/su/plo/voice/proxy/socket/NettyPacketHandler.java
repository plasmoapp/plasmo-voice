package su.plo.voice.proxy.socket;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import su.plo.lib.api.proxy.connection.MinecraftProxyServerConnection;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.proxy.connection.CancelForwardingException;
import su.plo.voice.socket.NettyPacketUdp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class NettyPacketHandler extends SimpleChannelInboundHandler<NettyPacketUdp> {

    private final BaseVoiceProxy voiceProxy;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyPacketUdp nettyPacket) throws Exception {
        PacketUdp packet = nettyPacket.getPacketUdp();

        UUID secret = packet.getSecret();

        if (voiceProxy.getUdpConnectionManager().getConnectionByAnySecret(secret)
                .map(connection -> sendPacket(ctx, nettyPacket, connection))
                .orElse(false)
        ) return;

        voiceProxy.getDebugLogger().log("Connection with secret {}", secret);

        Optional<UUID> playerId = voiceProxy.getUdpConnectionManager().getPlayerIdBySecret(secret);
        if (!playerId.isPresent()) {
            voiceProxy.getDebugLogger().log("Player not found by secret {}", secret);
            return;
        }

        Optional<UUID> remoteSecret = voiceProxy.getUdpConnectionManager().getRemoteSecretByPlayerId(playerId.get());
        if (!remoteSecret.isPresent()) {
            voiceProxy.getDebugLogger().log("Remote secret not found by player id {}", playerId.get());
            return;
        }
        voiceProxy.getDebugLogger().log("{} remote secret: {}", playerId, remoteSecret);

        Optional<VoiceProxyPlayer> player = voiceProxy.getPlayerManager().getPlayerById(playerId.get());
        if (!player.isPresent()) return;

        Optional<MinecraftProxyServerConnection> playerServer = player.get().getInstance().getServer();
        if (!playerServer.isPresent()) return;

        Optional<RemoteServer> remoteServer = voiceProxy.getRemoteServerManager()
                .getServer(playerServer.get().getServerInfo().getName());
        if (!remoteServer.isPresent()) return;
        voiceProxy.getDebugLogger().log("{} server: {}", player.get().getInstance().getName(), remoteServer.get());

        NettyUdpProxyConnection connection = new NettyUdpProxyConnection(
                voiceProxy,
                (NioDatagramChannel) ctx.channel(),
                player.get(),
                secret
        );
        connection.setRemoteSecret(remoteSecret.get());
        connection.setRemoteServer(remoteServer.get());
        connection.setRemoteAddress(nettyPacket.getDatagramPacket().sender());
        voiceProxy.getUdpConnectionManager().addConnection(connection);

        sendPacket(ctx, nettyPacket, connection);
    }

    private boolean sendPacket(ChannelHandlerContext ctx, NettyPacketUdp nettyPacket, UdpProxyConnection connection) {
        if (connection.getRemoteServer() == null) return false;

        InetSocketAddress sender = nettyPacket.getDatagramPacket().sender();
        InetSocketAddress receiver;
        UUID receiverSecret;

        if (!connection.getRemoteServer().getAddress().equals(sender)) {
            receiver = connection.getRemoteServer().getAddress();
            receiverSecret = connection.getRemoteSecret();

            if (!Objects.equals(connection.getRemoteAddress(), sender)) {
                connection.setRemoteAddress(nettyPacket.getDatagramPacket().sender());
            }

            // handle packet
            try {
                connection.handlePacket(nettyPacket.getPacketUdp().getPacket());
            } catch (CancelForwardingException ignored) {
                return true;
            } catch (IOException e) {
                voiceProxy.getDebugLogger().log("Failed to decode packet", e);
            } catch (ClassCastException e) {
                voiceProxy.getDebugLogger().log(
                        "Packet {} was received from remote server: {}; connection remote server: {}",
                        nettyPacket.getPacketUdp(),
                        sender,
                        connection.getRemoteServer().getAddress()
                );

                if (voiceProxy.getDebugLogger().enabled()) {
                    e.printStackTrace();
                }
            }
        } else {
            receiver = connection.getRemoteAddress();
            receiverSecret = connection.getSecret();
        }

        // rewrite to backend server
        ctx.channel().writeAndFlush(new DatagramPacket(
                Unpooled.wrappedBuffer(PacketUdpCodec.replaceSecret(
                        nettyPacket.getPacketData(),
                        receiverSecret
                )),
                receiver
        ));
        return true;
    }
}

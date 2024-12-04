package su.plo.voice.proxy.socket;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import su.plo.slib.api.proxy.connection.McProxyServerConnection;
import su.plo.voice.BaseVoice;
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

        BaseVoice.DEBUG_LOGGER.log("Connection with secret {}", secret);

        Optional<UUID> playerId = voiceProxy.getUdpConnectionManager().getPlayerIdByProxySecret(secret);
        if (!playerId.isPresent()) {
            BaseVoice.DEBUG_LOGGER.log("Player not found by secret {}", secret);
            return;
        }

        Optional<UUID> remoteSecret = voiceProxy.getUdpConnectionManager().getRemoteSecretByPlayerId(playerId.get());
        if (!remoteSecret.isPresent()) {
            BaseVoice.DEBUG_LOGGER.log("Remote secret not found by player id {}", playerId.get());
            return;
        }
        BaseVoice.DEBUG_LOGGER.log("{} remote secret: {}", playerId, remoteSecret);

        Optional<VoiceProxyPlayer> player = voiceProxy.getPlayerManager().getPlayerById(playerId.get());
        if (!player.isPresent()) return;

        McProxyServerConnection playerServer = player.get().getInstance().getServer();
        if (playerServer == null) return;

        Optional<RemoteServer> remoteServer = voiceProxy.getRemoteServerManager()
                .getServer(playerServer.getServerInfo().getName());
        if (!remoteServer.isPresent()) return;

        BaseVoice.DEBUG_LOGGER.log("{} server: {}", player.get().getInstance().getName(), remoteServer.get());

        if (!remoteServer.get().isAesEncryptionKeySet()) {
            BaseVoice.LOGGER.warn(
                    "AES encryption for server {} ({}) is not present. You need to set up the forwarding secret on backend servers: https://plasmovoice.com/docs/server/proxy/#specify-the-forwarding-secret",
                    remoteServer.get(),
                    player.get().getInstance().getName()
            );
            return;
        }

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
        if (!connection.getRemoteServer().isPresent()) return false;

        RemoteServer remoteServer = connection.getRemoteServer().get();

        InetSocketAddress sender = nettyPacket.getDatagramPacket().sender();
        InetSocketAddress receiver;
        UUID receiverSecret;

        if (!remoteServer.getAddress().equals(sender)) {
            receiver = remoteServer.getAddress();
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
                BaseVoice.DEBUG_LOGGER.log("Failed to decode packet", e);
            } catch (ClassCastException e) {
                BaseVoice.DEBUG_LOGGER.log(
                        "Packet {} was received from remote server: {}; connection remote server: {}",
                        nettyPacket.getPacketUdp(),
                        sender,
                        remoteServer.getAddress()
                );

                if (BaseVoice.DEBUG_LOGGER.enabled()) {
                    e.printStackTrace();
                }
            }
        } else {
            receiver = connection.getRemoteAddress();
            receiverSecret = connection.getSecret();

            if (!connection.isConnected()) return true;
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

package su.plo.voice.proxy.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.proxy.connection.McProxyServerConnection;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.proxy.connection.CancelForwardingException;
import su.plo.voice.proxy.server.VoiceRemoteServer;
import su.plo.voice.socket.NettyPacketUdp;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class NettyPacketHandler extends SimpleChannelInboundHandler<NettyPacketUdp> {

    private final BaseVoiceProxy voiceProxy;
    private final EventLoopGroup loopGroup;
    private final Class<? extends DatagramChannel> channelClass;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyPacketUdp nettyPacket) throws Exception {
        PacketUdp packet = nettyPacket.getPacketUdp();

        UUID secret = packet.getSecret();

        Optional<UdpProxyConnection> existingConnection = voiceProxy.getUdpConnectionManager().getConnectionBySecret(secret);
        if (existingConnection.isPresent()) {
            if (existingConnection.get() instanceof NettyUdpProxyConnection) {
                forwardPacket(nettyPacket, (NettyUdpProxyConnection) existingConnection.get());
            }
            return;
        }

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

        if (!remoteServer.get().isAesEncryptionKeySet() && System.getProperty("plasmovoice.skip_aes_server_check", "false").equals("true")) {
            ((VoiceRemoteServer) remoteServer.get()).setAesEncryptionKeySet(true);
            remoteServer.get().getAddress(true);
        } else if (!remoteServer.get().isAesEncryptionKeySet()) {
            BaseVoice.LOGGER.warn(
                    "AES encryption for server {} ({}) is not present. You need to set up the forwarding secret on backend servers: https://plasmovoice.com/docs/server/proxy/#specify-the-forwarding-secret",
                    remoteServer.get(),
                    player.get().getInstance().getName()
            );
            return;
        }

        NettyUdpProxyConnection connection = new NettyUdpProxyConnection(
                voiceProxy,
                (DatagramChannel) ctx.channel(),
                player.get(),
                secret,
                loopGroup,
                channelClass
        );
        connection.setRemoteSecret(remoteSecret.get());
        connection.setRemoteAddress(nettyPacket.getDatagramPacket().sender());
        if (packet.getPacketUntyped() instanceof PingPacket) {
            PingPacket pingPacket = (PingPacket) packet.getPacketUntyped();
            if (pingPacket.getServerIp() != null) {
                connection.setConnectionAddress(InetSocketAddress.createUnresolved(pingPacket.getServerIp(), pingPacket.getServerPort()));
            }
        }
        connection.setRemoteServer(remoteServer.get());
        voiceProxy.getUdpConnectionManager().addConnection(connection);

        forwardPacket(nettyPacket, connection);
    }

    private void forwardPacket(@NotNull NettyPacketUdp nettyPacket, @NotNull NettyUdpProxyConnection connection) {
        InetSocketAddress sender = nettyPacket.getDatagramPacket().sender();
        if (!Objects.equals(connection.getRemoteAddress(), sender)) {
            connection.setRemoteAddress(sender);
        }

        try {
            connection.handlePacket(nettyPacket.getPacketUdp().getPacket());
        } catch (CancelForwardingException ignored) {
            return;
        } catch (Throwable e) {
            BaseVoice.DEBUG_LOGGER.log("Failed to decode packet", e);
        }

        connection.sendPacketToRemoteServer(nettyPacket);
    }
}

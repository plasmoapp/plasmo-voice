package su.plo.voice.server.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent;
import su.plo.voice.api.server.event.connection.UdpPacketReceivedEvent;
import su.plo.voice.api.server.event.connection.UdpPacketSendEvent;
import su.plo.voice.api.server.event.connection.UdpPacketSentEvent;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.socket.ByteBufDataOutput;

import java.net.InetSocketAddress;
import java.util.UUID;

@ToString(of = {"channel", "secret", "player", "keepAlive", "sentKeepAlive"})
public final class NettyUdpServerConnection implements UdpServerConnection, ServerPacketUdpHandler {

    private final BaseVoiceServer voiceServer;
    private final DatagramChannel channel;

    @Getter
    private InetSocketAddress remoteAddress;
    @Getter
    @Setter
    private InetSocketAddress connectionAddress;
    @Getter
    private final UUID secret;
    @Getter
    private final VoiceServerPlayer player;
    @Getter
    private long keepAlive = System.currentTimeMillis();
    @Getter
    @Setter
    private long sentKeepAlive;
    @Getter
    private long lastReceivedPacketTimestamp = System.currentTimeMillis();

    @Getter
    private boolean connected = true;

    public NettyUdpServerConnection(@NotNull BaseVoiceServer voiceServer,
                                    @NotNull DatagramChannel channel,
                                    @NotNull UUID secret,
                                    @NotNull VoiceServerPlayer player) {
        this.voiceServer = voiceServer;
        this.channel = channel;
        this.secret = secret;
        this.player = player;
    }

    @Override
    public void setRemoteAddress(@NotNull InetSocketAddress remoteAddress) {
        BaseVoice.DEBUG_LOGGER.log("Set remote address for {} from {} to {}",
                player.getInstance().getName(),
                this.remoteAddress, remoteAddress
        );
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        if (voiceServer.getEventBus().hasListener(UdpPacketSendEvent.class)) {
            UdpPacketSendEvent event = new UdpPacketSendEvent(this, packet);
            if (!voiceServer.getEventBus().fire(event)) return;
        }

        ByteBuf buf = channel.alloc().ioBuffer();
        try {
            PacketUdpCodec.encodeThrowing(packet, secret, new ByteBufDataOutput(buf));
        } catch (Throwable e) {
            buf.release();
            BaseVoice.DEBUG_LOGGER.log("Failed to encode packet", e);
            return;
        }

        channel.writeAndFlush(new DatagramPacket(buf, remoteAddress), channel.voidPromise());

        if (voiceServer.getEventBus().hasListener(UdpPacketSentEvent.class)) {
            voiceServer.getEventBus().fire(new UdpPacketSentEvent(this, packet));
        }
    }

    @Override
    public void handlePacket(Packet<ServerPacketUdpHandler> packet) {
        if (voiceServer.getEventBus().hasListener(UdpPacketReceivedEvent.class)) {
            UdpPacketReceivedEvent event = new UdpPacketReceivedEvent(this, packet);
            if (!voiceServer.getEventBus().fire(event)) return;
        }

        packet.handle(this);
        this.lastReceivedPacketTimestamp = System.currentTimeMillis();
    }

    @Override
    public void disconnect() {
        connected = false;

        voiceServer.getTcpPacketManager().broadcastPlayerDisconnect(player);
    }

    @Override
    public void handle(@NotNull PingPacket packet) {
        this.keepAlive = System.currentTimeMillis();
    }

    @Override
    public void handle(@NotNull CustomPacket packet) {
    }

    @Override
    public void handle(@NotNull PlayerAudioPacket packet) {
        if (voiceServer.getMuteManager().getMute(player.getInstance().getUuid()).isPresent()) return;
        if (player.isMicrophoneMuted()) return;

        voiceServer.getEventBus().fire(new PlayerSpeakEvent(player, packet));
    }
}

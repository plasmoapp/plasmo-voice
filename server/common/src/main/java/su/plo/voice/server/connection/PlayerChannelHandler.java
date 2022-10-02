package su.plo.voice.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.audio.source.ServerPlayerSource;
import su.plo.voice.api.server.audio.source.ServerSourceManager;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.event.connection.TcpPacketReceivedEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.tcp.serverbound.*;
import su.plo.voice.server.player.VoiceServerPlayer;
import su.plo.voice.util.VersionUtil;

import java.util.Optional;

public final class PlayerChannelHandler implements ServerPacketTcpHandler {

    private final PlasmoVoiceServer voiceServer;
    private final TcpServerConnectionManager tcpConnections;
    private final ServerActivationManager activations;
    private final ServerSourceLineManager lines;
    private final ServerSourceManager sources;
    private final VoicePlayer player;

    public PlayerChannelHandler(@NotNull PlasmoVoiceServer voiceServer,
                                @NotNull VoicePlayer player) {
        this.voiceServer = voiceServer;
        this.tcpConnections = voiceServer.getTcpConnectionManager();
        this.activations = voiceServer.getActivationManager();
        this.lines = voiceServer.getSourceLineManager();
        this.sources = voiceServer.getSourceManager();
        this.player = player;
    }

    public void handlePacket(Packet<PacketHandler> packet) {
        if (!voiceServer.getUdpServer().isPresent()) return;

        TcpPacketReceivedEvent event = new TcpPacketReceivedEvent(player, packet);
        voiceServer.getEventBus().call(event);
        if (event.isCancelled()) return;

        packet.handle(this);
    }

    @Override
    public void handle(@NotNull PlayerInfoPacket packet) {
        int[] serverVersion = VersionUtil.parseVersion(voiceServer.getVersion());
        int[] clientVersion = VersionUtil.parseVersion(packet.getVersion());

        if (clientVersion[0] > serverVersion[0]) {
            player.getInstance().sendMessage(TextComponent.translatable(
                    "message.plasmovoice.version_not_supported",
                    String.format("%d.X.X", serverVersion[0])
            ));
            return;
        } else if (clientVersion[0] < serverVersion[0]) {
            player.getInstance().sendMessage(TextComponent.translatable(
                    "message.plasmovoice.min_version",
                    String.format("%d.X.X", serverVersion[0])
            ));
            return;
        }

        tcpConnections.sendConfigInfo(player);
        tcpConnections.sendPlayerList(player);

        tcpConnections.broadcast(new PlayerInfoUpdatePacket(player.getInfo()));
    }

    @Override
    public void handle(@NotNull PlayerStatePacket packet) {
        VoiceServerPlayer voicePlayer = (VoiceServerPlayer) player;
        voicePlayer.setVoiceDisabled(packet.isVoiceDisabled());
        voicePlayer.setMicrophoneMuted(packet.isMicrophoneMuted());

        tcpConnections.broadcastPlayerInfoUpdate(player);
    }

    @Override
    public void handle(@NotNull PlayerActivationDistancesPacket packet) {
        VoiceServerPlayer voicePlayer = (VoiceServerPlayer) player;
        packet.getDistanceByActivationId().forEach((activationId, distance) -> {
            Optional<ServerActivation> activation = voiceServer.getActivationManager().getActivationById(activationId);
            if (!activation.isPresent()) return;

            voicePlayer.setActivationDistance(activation.get(), distance);
        });
    }

    @Override
    public void handle(@NotNull PlayerAudioEndPacket packet) {
        Optional<ServerActivation> activation = activations.getActivationById(VoiceActivation.PROXIMITY_ID);
        if (!activation.isPresent()) return;

        Optional<ServerSourceLine> sourceLine = lines.getLineById(VoiceSourceLine.PROXIMITY_ID);
        if (!sourceLine.isPresent()) return;

        ServerPlayerSource source = sources.createPlayerSource(
                voiceServer,
                player,
                sourceLine.get(),
                "opus",
                activation.get().isStereoSupported()
        );

        SourceAudioEndPacket sourcePacket = new SourceAudioEndPacket(source.getId(), packet.getSequenceNumber());
        source.sendPacket(sourcePacket, packet.getDistance());
    }

    @Override
    public void handle(@NotNull SourceInfoRequestPacket packet) {
        Optional<ServerAudioSource> source = voiceServer.getSourceManager().getSourceById(packet.getSourceId());
        if (!source.isPresent()) return;

        player.sendPacket(new SourceInfoPacket(source.get().getInfo()));
    }

    private boolean selfFilter(VoicePlayer player) {
        return player.equals(this.player);
    }
}

package su.plo.voice.server.connection;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.audio.source.ServerPlayerSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.tcp.serverbound.*;
import su.plo.voice.server.player.BaseVoicePlayer;
import su.plo.voice.util.VersionUtil;

import java.util.Optional;

@RequiredArgsConstructor
public final class PlayerChannelHandler implements ServerPacketTcpHandler {

    private final PlasmoVoiceServer voiceServer;
    private final VoicePlayer player;

    @Override
    public void handle(@NotNull PlayerInfoPacket packet) {
        int[] serverVersion = VersionUtil.parseVersion(voiceServer.getVersion());
        int[] clientVersion = VersionUtil.parseVersion(packet.getVersion());

        if (clientVersion[0] > serverVersion[0]) {
            player.sendTranslatableMessage(
                    "message.plasmovoice.version_not_supported",
                    String.format("%d.X.X", serverVersion[0])
            );
            return;
        } else if (clientVersion[0] < serverVersion[0]) {
            player.sendTranslatableMessage(
                    "message.plasmovoice.min_version",
                    String.format("%d.X.X", serverVersion[0])
            );
            return;
        }

        voiceServer.getTcpConnectionManager().sendConfigInfo(player);
        voiceServer.getTcpConnectionManager().sendPlayerList(player);

        // todo: broadcast connected player
    }

    @Override
    public void handle(@NotNull PlayerStatePacket packet) {
        BaseVoicePlayer voicePlayer = (BaseVoicePlayer) player;
        voicePlayer.setVoiceDisabled(packet.isVoiceDisabled());
        voicePlayer.setMicrophoneMuted(packet.isMicrophoneMuted());

        // todo: broadcast player state update packet

//        voiceServer.getTcpConnectionManager().broadcast();
    }

    @Override
    public void handle(@NotNull PlayerAudioEndPacket packet) {
        ServerPlayerSource source = voiceServer.getSourceManager().getOrCreatePlayerSource(voiceServer, player, "opus", false);

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

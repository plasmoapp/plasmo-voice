package su.plo.voice.server.connection;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerInfoPacket;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerStatePacket;
import su.plo.voice.proto.packets.tcp.serverbound.ServerPacketTcpHandler;
import su.plo.voice.util.VersionUtil;

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
                    "message.plasmo_voice.version_not_supported",
                    String.format("%d.X.X", serverVersion[0])
            );
            return;
        } else if (clientVersion[0] < serverVersion[0]) {
            player.sendTranslatableMessage(
                    "message.plasmo_voice.min_version",
                    String.format("%d.X.X", serverVersion[0])
            );
            return;
        }

        voiceServer.getTcpConnectionManager().sendConfigInfo(player);
        voiceServer.getTcpConnectionManager().sendPlayerList(player);
    }

    @Override
    public void handle(@NotNull PlayerStatePacket packet) {
    }

    @Override
    public void handle(@NotNull PlayerAudioEndPacket packet) {

    }
}

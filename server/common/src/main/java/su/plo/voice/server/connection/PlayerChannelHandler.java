package su.plo.voice.server.connection;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.tcp.serverbound.ServerPacketTcpHandler;

@RequiredArgsConstructor
public final class PlayerChannelHandler implements ServerPacketTcpHandler {

    private final PlasmoVoiceServer voiceServer;
    private final VoicePlayer player;

    @Override
    public void handle(@NotNull PlayerAudioEndPacket packet) {

    }
}

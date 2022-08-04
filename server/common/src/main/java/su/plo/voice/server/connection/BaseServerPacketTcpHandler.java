package su.plo.voice.server.connection;

import lombok.AllArgsConstructor;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.serverbound.ServerPacketTcpHandler;
import su.plo.voice.server.BaseVoiceServer;

import java.util.List;

@AllArgsConstructor
public abstract class BaseServerPacketTcpHandler implements ServerPacketTcpHandler {

    private final PlasmoVoiceServer server;

    public void handleRegisterChannels(List<String> channels, VoicePlayer player) {
        if (channels.contains(BaseVoiceServer.CHANNEL_STRING)) {
            server.getTcpConnectionManager().connect(player);
        }
    }
}

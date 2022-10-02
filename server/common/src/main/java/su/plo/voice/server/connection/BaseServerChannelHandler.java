package su.plo.voice.server.connection;

import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.player.VoiceServerPlayer;

import java.util.List;

public abstract class BaseServerChannelHandler {

    protected final PlasmoVoiceServer voiceServer;

    protected BaseServerChannelHandler(PlasmoVoiceServer voiceServer) {
        this.voiceServer = voiceServer;
    }

    protected void handleRegisterChannels(List<String> channels, VoicePlayer player) {
        if (!voiceServer.getUdpServer().isPresent()) return;

        if (channels.contains(BaseVoiceServer.CHANNEL_STRING)) {
            voiceServer.getTcpConnectionManager().connect(player);

            ((VoiceServerPlayer) player).setModLoader(
                    channels.contains("fml:handshake")
                            ? PlayerModLoader.FORGE
                            : PlayerModLoader.FABRIC
            );
        }
    }
}

package su.plo.voice.paper.connection;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.connection.BaseServerServiceChannelHandler;

import java.io.IOException;

public final class PaperServerServiceChannelHandler extends BaseServerServiceChannelHandler implements PluginMessageListener {

    public PaperServerServiceChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        super(voiceServer);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channelName, @NotNull Player player, @NotNull byte[] message) {
        try {
            VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().wrap(player);

            handlePacket(voicePlayer, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

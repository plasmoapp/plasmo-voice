package su.plo.voice.mod.server.connection;

import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.connection.BaseServerServiceChannelHandler;

import java.io.IOException;

public abstract class ModServerServiceChannelHandler extends BaseServerServiceChannelHandler {

    public ModServerServiceChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        super(voiceServer);
    }

    protected void receive(ServerPlayer player, FriendlyByteBuf buf) {
        byte[] data = ByteBufUtil.getBytes(buf.duplicate());

        try {
            VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().wrap(player);

            handlePacket(voicePlayer, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package su.plo.voice.server.connection;

import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.server.BaseVoiceServer;

//#if FABRIC
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
//#else
//$$ import net.minecraftforge.network.NetworkDirection;
//$$ import net.minecraftforge.network.NetworkEvent;
//#endif

import java.io.IOException;

public final class ModServerServiceChannelHandler
        extends BaseServerServiceChannelHandler
        //#if FABRIC
        implements ServerPlayNetworking.PlayChannelHandler
        //#endif
{

    public ModServerServiceChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        super(voiceServer);
    }

    private void receive(ServerPlayer player, FriendlyByteBuf buf) {
        byte[] data = ByteBufUtil.getBytes(buf.duplicate());

        try {
            VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().wrap(player);

            handlePacket(voicePlayer, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //#if FABRIC
    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        receive(player, buf);
    }
    //#else
    //$$ public void receive(@NotNull NetworkEvent event) {
    //$$     NetworkEvent.Context context = event.getSource().get();
    //$$     if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER || event.getPayload() == null) return;
    //$$     receive(context.getSender(), event.getPayload());
    //$$     context.setPacketHandled(true);
    //$$ }
    //#endif
}

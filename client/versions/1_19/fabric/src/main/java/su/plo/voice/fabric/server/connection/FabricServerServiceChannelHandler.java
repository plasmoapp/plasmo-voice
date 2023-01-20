package su.plo.voice.fabric.server.connection;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.mod.server.connection.ModServerServiceChannelHandler;
import su.plo.voice.server.BaseVoiceServer;

public final class FabricServerServiceChannelHandler extends ModServerServiceChannelHandler implements ServerPlayNetworking.PlayChannelHandler {

    public FabricServerServiceChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        super(voiceServer);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        receive(player, buf);
    }
}

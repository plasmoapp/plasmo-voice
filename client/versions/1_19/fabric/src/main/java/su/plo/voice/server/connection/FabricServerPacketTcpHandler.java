package su.plo.voice.server.connection;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import su.plo.voice.api.server.PlasmoVoiceServer;

public final class FabricServerPacketTcpHandler extends BaseServerPacketTcpHandler implements ServerPlayNetworking.PlayChannelHandler {

    public FabricServerPacketTcpHandler(PlasmoVoiceServer server) {
        super(server);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {

    }
}

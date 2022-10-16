package su.plo.voice.fabric.server.connection;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.mod.server.connection.ModServerChannelHandler;

import java.util.List;
import java.util.stream.Collectors;

public final class FabricServerChannelHandler extends ModServerChannelHandler implements ServerPlayNetworking.PlayChannelHandler, S2CPlayChannelEvents.Register {

    public FabricServerChannelHandler(PlasmoVoiceServer voiceServer) {
        super(voiceServer);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        receive(player, buf);
    }

    @Override
    public void onChannelRegister(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server, List<ResourceLocation> channels) {
        voiceServer.getPlayerManager().getPlayerById(handler.getPlayer().getUUID()).ifPresent(player ->
                handleRegisterChannels(
                        channels.stream().map(ResourceLocation::toString).collect(Collectors.toList()),
                        player
                )
        );
    }
}

package su.plo.voice.fabric.client.connection;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.mod.client.connection.ModClientChannelHandler;

public final class FabricClientChannelHandler extends ModClientChannelHandler implements ClientPlayNetworking.PlayChannelHandler {

    public FabricClientChannelHandler(@NotNull BaseVoiceClient voiceClient,
                                      @NotNull MinecraftClientLib minecraft) {
        super(voiceClient, minecraft);
    }

    @Override
    public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        receive(handler.getConnection(), buf);
    }
}

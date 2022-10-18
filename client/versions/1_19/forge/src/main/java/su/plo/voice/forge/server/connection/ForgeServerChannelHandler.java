package su.plo.voice.forge.server.connection;

import io.netty.util.AsciiString;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.mod.server.connection.ModServerChannelHandler;
import su.plo.voice.server.BaseVoiceServer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ForgeServerChannelHandler extends ModServerChannelHandler {

    public static ForgeServerChannelHandler INSTANCE;

    public ForgeServerChannelHandler(@NotNull BaseVoiceServer voiceServer,
                                     @NotNull EventNetworkChannel channel) {
        super(voiceServer);

        INSTANCE = this;
        channel.addListener(this::receive);
    }

    public void onChannelRegister(@NotNull ServerPlayer serverPlayer, @NotNull ServerboundCustomPayloadPacket packet) {
        FriendlyByteBuf buf = packet.getData();

        List<ResourceLocation> channels = new ArrayList<>();
        StringBuilder active = new StringBuilder();

        while (buf.isReadable()) {
            byte b = buf.readByte();

            if (b != 0) {
                active.append(AsciiString.b2c(b));
            } else {
                try {
                    channels.add(new ResourceLocation(active.toString()));
                } catch (ResourceLocationException ex) {
                    continue;
                }
                active = new StringBuilder();
            }
        }

        VoicePlayer player = voiceServer.getPlayerManager().wrap(serverPlayer);
        handleRegisterChannels(
                channels.stream().map(ResourceLocation::toString).collect(Collectors.toList()),
                player
        );
    }

    private void receive(@NotNull NetworkEvent event) {
        NetworkEvent.Context context = event.getSource().get();
        if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER || event.getPayload() == null) return;
        receive(context.getSender(), event.getPayload());
    }
}

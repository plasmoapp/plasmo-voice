package su.plo.voice.forge.client.connection;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.mod.client.connection.ModClientChannelHandler;

public final class ForgeClientChannelHandler extends ModClientChannelHandler {

    public ForgeClientChannelHandler(@NotNull BaseVoiceClient voiceClient,
                                     @NotNull MinecraftClientLib minecraft,
                                     @NotNull EventNetworkChannel channel) {
        super(voiceClient, minecraft);

        channel.addListener(this::receive);
    }

    private void receive(@NotNull NetworkEvent event) {
        NetworkEvent.Context context = event.getSource().get();
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT || event.getPayload() == null) return;
        receive(context.getNetworkManager(), event.getPayload());
    }
}

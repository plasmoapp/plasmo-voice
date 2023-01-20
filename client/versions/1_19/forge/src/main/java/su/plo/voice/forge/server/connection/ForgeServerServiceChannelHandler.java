package su.plo.voice.forge.server.connection;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.mod.server.connection.ModServerServiceChannelHandler;
import su.plo.voice.server.BaseVoiceServer;

public final class ForgeServerServiceChannelHandler extends ModServerServiceChannelHandler {

    public ForgeServerServiceChannelHandler(@NotNull BaseVoiceServer voiceServer,
                                            @NotNull EventNetworkChannel channel) {
        super(voiceServer);

        channel.addListener(this::receive);
    }

    private void receive(@NotNull NetworkEvent event) {
        NetworkEvent.Context context = event.getSource().get();
        if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER || event.getPayload() == null) return;
        receive(context.getSender(), event.getPayload());
    }
}

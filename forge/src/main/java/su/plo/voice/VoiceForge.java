package su.plo.voice;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.event.EventNetworkChannel;
import su.plo.voice.client.VoiceClientForge;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.VoiceServerForge;
import su.plo.voice.server.network.ServerNetworkHandlerForge;

@Mod("plasmo_voice")
public class VoiceForge {
    public VoiceForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        (new VoiceClientForge()).initialize();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new VoiceServerForge());

        EventNetworkChannel channel = NetworkRegistry.newEventChannel(
                VoiceServer.PLASMO_VOICE,
                () -> NetworkRegistry.ACCEPTVANILLA,
                NetworkRegistry.ACCEPTVANILLA::equals,
                NetworkRegistry.ACCEPTVANILLA::equals
        );

        channel.addListener(e -> {
            if (e.getPayload() == null) {
                return;
            }

            if (e.getSource().get().getDirection().equals(NetworkDirection.PLAY_TO_CLIENT)) {
                VoiceClientForge.getNetwork().handle(e.getSource().get().getNetworkManager(), e.getPayload());
            } else {
                ((ServerNetworkHandlerForge) VoiceServer.getNetwork()).handle(e.getSource().get().getSender(), e.getPayload());
            }
        });
    }
}

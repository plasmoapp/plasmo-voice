package su.plo.voice;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.VoiceClientForge;
import su.plo.voice.server.VoiceServerForge;

@Mod("plasmovoice")
public class VoiceForge {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            VoiceClient.PLASMO_VOICE,
            () -> VoiceClient.PROTOCOL_VERSION,
            VoiceClient.PROTOCOL_VERSION::equals,
            VoiceClient.PROTOCOL_VERSION::equals
    );

    public VoiceForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        (new VoiceClientForge()).initialize();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new VoiceServerForge());
    }
}

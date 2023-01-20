package su.plo.voice.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import su.plo.voice.forge.client.ForgeVoiceClient;
import su.plo.voice.forge.server.ForgeVoiceServer;
import su.plo.voice.mod.server.ModVoiceServer;

@Mod("plasmovoice")
public final class ForgeVoice {

    private EventNetworkChannel channel;

    public ForgeVoice() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        ForgeVoiceClient voiceClient = new ForgeVoiceClient();
        MinecraftForge.EVENT_BUS.register(voiceClient);
        voiceClient.onInitialize(channel);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        this.channel = NetworkRegistry.newEventChannel(
                ModVoiceServer.CHANNEL,
                () -> NetworkRegistry.ACCEPTVANILLA,
                NetworkRegistry.ACCEPTVANILLA::equals,
                NetworkRegistry.ACCEPTVANILLA::equals
        );

        EventNetworkChannel serviceChannel = NetworkRegistry.newEventChannel(
                ModVoiceServer.SERVICE_CHANNEL,
                () -> NetworkRegistry.ACCEPTVANILLA,
                NetworkRegistry.ACCEPTVANILLA::equals,
                NetworkRegistry.ACCEPTVANILLA::equals
        );

        ForgeVoiceServer voiceServer = new ForgeVoiceServer();
        MinecraftForge.EVENT_BUS.register(voiceServer);
        voiceServer.onInitialize(channel, serviceChannel);
    }
}

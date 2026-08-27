package su.plo.voice;

import su.plo.voice.server.ModVoiceServer;
import su.plo.voice.util.version.PlatformLoader;

//#if FABRIC

import net.fabricmc.api.ModInitializer;

//#elseif NEOFORGE
//$$
//$$ import net.neoforged.bus.api.IEventBus;
//$$ import net.neoforged.bus.api.SubscribeEvent;
//$$ import net.neoforged.fml.ModContainer;
//$$ import net.neoforged.fml.common.Mod;
//$$ import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
//$$ import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
//$$ import net.neoforged.neoforge.common.NeoForge;
//$$ import org.jetbrains.annotations.NotNull;
//$$ import su.plo.slib.mod.ModServerLib;
//$$ import su.plo.slib.mod.channel.ModChannelManager;
//$$ import su.plo.voice.client.ModVoiceClient;
//$$
//#endif

//#if NEOFORGE
//$$ @Mod("plasmovoice")
//#endif
public final class ModVoice
        //#if FABRIC
        implements ModInitializer
        //#endif
{
    //#if NEOFORGE
    //$$ public ModVoice(IEventBus modBus, ModContainer container) {
    //$$     modBus.register(this);
    //$$     modBus.register(ModServerLib.INSTANCE.getChannelManager());
    //$$
    //$$     ModChannelManager.Companion.getOrRegisterCodec(ModVoiceServer.CHANNEL);
    //$$     ModChannelManager.Companion.getOrRegisterCodec(ModVoiceServer.SERVICE_CHANNEL);
    //$$     ModChannelManager.Companion.getOrRegisterCodec(ModVoiceServer.FLAG_CHANNEL);
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onClientSetup(@NotNull FMLClientSetupEvent event) {
    //$$     ModVoiceClient voiceClient = new ModVoiceClient();
    //$$     NeoForge.EVENT_BUS.register(voiceClient);
    //$$     voiceClient.onInitialize();
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onCommonSetup(@NotNull FMLCommonSetupEvent event) {
    //$$     ModVoiceServer voiceServer = new ModVoiceServer(PlatformLoader.NEO_FORGE);
    //$$     voiceServer.onInitialize();
    //$$ }
    //#endif

    //#if FABRIC
    @Override
    public void onInitialize() {
        ModVoiceServer voiceServer = new ModVoiceServer(PlatformLoader.FABRIC);
        voiceServer.onInitialize();
    }
    //#endif
}

package su.plo.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import su.plo.slib.api.logging.McLoggerFactory;
import su.plo.slib.mod.logging.Log4jLogger;
import su.plo.voice.client.gui.settings.VoiceScreens;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.audio.device.AlInputDeviceFactory;
import su.plo.voice.client.audio.device.AlOutputDeviceFactory;
import su.plo.voice.client.audio.device.JavaxInputDeviceFactory;
import su.plo.voice.client.connection.ModClientChannelHandler;
import su.plo.voice.client.event.key.KeyPressedEvent;
import su.plo.voice.client.render.ModHudRenderer;
import su.plo.voice.client.render.ModLevelRenderer;
import su.plo.voice.util.version.PlatformLoader;

//#if FABRIC

import su.plo.voice.server.ModVoiceServer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

//#if MC<12109
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
//#endif

//#if MC>=12005
//$$ import su.plo.slib.mod.channel.ByteArrayCodec;
//$$ import su.plo.slib.mod.channel.ModChannelManager;
//#endif

//#elseif FORGE

//$$ import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent;
//$$
//$$ import net.minecraftforge.network.event.EventNetworkChannel;

//#if MC>=12100
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#else
//$$ import net.minecraftforge.client.event.RenderGuiOverlayEvent;
//#endif

//#if MC>=11900
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import net.minecraftforge.api.distmarker.Dist;
//$$ import net.minecraftforge.client.event.RenderLevelStageEvent;
//$$ import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

//#if MC<12100
//$$ import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
//#endif

//#else
//$$ import net.minecraftforge.client.ClientRegistry;
//$$ import net.minecraftforge.client.event.RenderLevelLastEvent;
//#endif

//#elseif NEOFORGE

//$$ import su.plo.slib.mod.channel.ModChannelManager;
//$$ import su.plo.voice.server.ModVoiceServer;
//$$ import net.neoforged.api.distmarker.Dist;
//$$ import net.neoforged.bus.api.SubscribeEvent;
//$$ import net.neoforged.fml.common.EventBusSubscriber;
//$$ import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
//$$ import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
//$$ import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
//$$ import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
//$$ import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

//#endif

//#if MC>=12109
//$$ import su.plo.lib.mod.client.ResourceLocationUtil;
//#endif

import java.io.File;
import java.util.Optional;

public final class ModVoiceClient extends BaseVoiceClient
        //#if FABRIC
        implements ClientModInitializer
        //#endif
{

    // static instance is used for access from mixins
    public static ModVoiceClient INSTANCE;

    private final String modId = "plasmovoice";

    public static final KeyMapping MENU_KEY = new KeyMapping(
            "key.plasmovoice.settings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            //#if MC>=12109
            //$$ KeyMapping.Category.register(ResourceLocationUtil.mod("name"))
            //#else
            "Plasmo Voice"
            //#endif
    );

    @Getter
    private final ModHudRenderer hudRenderer;
    @Getter
    private final ModLevelRenderer levelRenderer;

    private final ModClientChannelHandler handler = new ModClientChannelHandler(this);

    public ModVoiceClient() {
        //#if FORGE
        //$$ super(PlatformLoader.FORGE);
        //#else
        super(PlatformLoader.FABRIC);
        //#endif

        McLoggerFactory.supplier = Log4jLogger::new;

        DeviceFactoryManager factoryManager = getDeviceFactoryManager();

        // OpenAL
        factoryManager.registerDeviceFactory(new AlOutputDeviceFactory(this));
        factoryManager.registerDeviceFactory(new AlInputDeviceFactory(this));

        // JavaX input
        getDeviceFactoryManager().registerDeviceFactory(new JavaxInputDeviceFactory(this));

        this.hudRenderer = new ModHudRenderer();
        this.levelRenderer = new ModLevelRenderer(this);

        INSTANCE = this;
        RenderUtil.getTextConverter().setLanguageSupplier(createLanguageSupplier());
    }

    @Override
    protected void onServerDisconnect() {
        super.onServerDisconnect();
        handler.close();
    }

    @EventSubscribe
    public void onKeyPressed(@NotNull KeyPressedEvent event) {
        if (Minecraft.getInstance().player == null) return;
        if (MENU_KEY.consumeClick()) VoiceScreens.INSTANCE.openSettings(this);
    }

    @Override
    public @NotNull File getConfigFolder() {
        return new File("config/" + modId);
    }

    @Override
    public Optional<ServerConnection> getServerConnection() {
        return handler.getConnection();
    }

    //#if FABRIC
    @Override
    public void onInitializeClient() {
        super.onInitialize();

        ClientLifecycleEvents.CLIENT_STOPPING.register((minecraft) -> onShutdown());
        HudRenderCallback.EVENT.register(hudRenderer::render);
        //#if MC<12109
        WorldRenderEvents.LAST.register(
                (context) -> levelRenderer.render(
                        context.world(),
                        context.matrixStack(),
                        //#if MC>=12100
                        //$$ context.tickCounter().getRealtimeDeltaTicks()
                        //#else
                        context.tickDelta()
                        //#endif
                )
        );
        //#endif
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onServerDisconnect());

        //#if MC>=12005
        //$$ ByteArrayCodec voiceChannelCodec = ModChannelManager.Companion.getOrRegisterCodec(ModVoiceServer.CHANNEL);
        //$$ ByteArrayCodec flagChannelCodec = ModChannelManager.Companion.getOrRegisterCodec(ModVoiceServer.FLAG_CHANNEL);
        //$$
        //$$ ClientPlayNetworking.registerGlobalReceiver(voiceChannelCodec.getType(), handler);
        //$$ ClientPlayNetworking.registerGlobalReceiver(flagChannelCodec.getType(), (payload, context) -> {});
        //#else
        ClientPlayNetworking.registerGlobalReceiver(ModVoiceServer.CHANNEL, handler);
        ClientPlayNetworking.registerGlobalReceiver(ModVoiceServer.FLAG_CHANNEL, (client, handler, buf, responseSender) -> {});
        //#endif

        KeyBindingHelper.registerKeyBinding(MENU_KEY);
    }

    //#elseif FORGE

    //$$ public void onInitialize(EventNetworkChannel channel) {
    //$$     channel.addListener(handler::receive);
             //#if MC<11900
             //$$ ClientRegistry.registerKeyBinding(MENU_KEY);
             //#endif
    //$$     super.onInitialize();
    //$$ }
    //$$
    //$$ @Override
    //$$ public void onShutdown() {
    //$$     super.onShutdown();
    //$$ }
    //$$
    //#if MC<12100

    //$$ @SubscribeEvent
    //$$ public void onOverlayRender(RenderGuiOverlayEvent.Post event) {
    //#if MC>=11900
    //$$     if (!event.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id())) return;
    //#else
    //$$     if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
    //#endif
    //$$
    //#if MC>=12000
    //$$     hudRenderer.render(event.getGuiGraphics(), event.getPartialTick());
    //#else
    //$$     hudRenderer.render(event.getPoseStack(), event.getPartialTick());
    //#endif
    //$$ }

    //#endif

    //$$
    //$$ @SubscribeEvent
    //$$ public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
    //$$     onServerDisconnect();
    //$$ }
    //$$
    //#if MC>=11900
    //$$ @SubscribeEvent
    //$$ public void onWorldRender(RenderLevelStageEvent event) {
    //$$     if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES ||
    //$$             Minecraft.getInstance().level == null
    //$$     ) return;
    //$$     levelRenderer.render(
    //$$             Minecraft.getInstance().level,
    //#if MC>=12100
    //$$             new PoseStack(),
    //#else
    //$$             event.getPoseStack(),
    //#endif
    //$$             event.getPartialTick()
    //$$     );
    //$$ }
    //$$
    //$$ @Mod.EventBusSubscriber(modid = "plasmovoice", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    //$$ public static class ModBusEvents {
    //$$
    //$$     @SubscribeEvent
    //$$     public static void onKeyMappingsRegister(RegisterKeyMappingsEvent event) {
    //$$         event.register(MENU_KEY);
    //$$     }
    //$$ }
    //#else
    //$$ @SubscribeEvent
    //$$ public void onWorldRender(RenderLevelLastEvent event) {
    //$$     levelRenderer.render(
    //$$             Minecraft.getInstance().level,
    //$$             event.getPoseStack(),
    //$$             event.getPartialTick()
    //$$     );
    //$$ }
    //#endif

    //#elseif NEOFORGE

    //$$ public void onInitialize() {
    //$$     super.onInitialize();
    //#if NEOFORGE
    //$$     ModChannelManager.Companion.registerClientHandler(ModVoiceServer.CHANNEL, handler);
    //#endif
    //$$ }
    //$$
    //$$ @Override
    //$$ public void onShutdown() {
    //$$     super.onShutdown();
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onOverlayRender(@NotNull RenderGuiLayerEvent.Post event) {
    //$$     if (!event.getName().equals(VanillaGuiLayers.CHAT)) return;
    //$$
    //$$     hudRenderer.render(event.getGuiGraphics(), event.getPartialTick());
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
    //$$     onServerDisconnect();
    //$$ }
    //$$
    //#if MC<12109
    //$$ @SubscribeEvent
    //#if MC>=12106
    //$$ public void onWorldRender(RenderLevelStageEvent.AfterParticles event) {
    //#else
    //$$ public void onWorldRender(RenderLevelStageEvent event) {
    //$$     if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
    //#endif
    //$$
    //$$     if (Minecraft.getInstance().level == null) return;
    //$$
    //$$     levelRenderer.render(
    //$$            Minecraft.getInstance().level,
    //$$            event.getPoseStack(),
    //#if MC>=12100
    //$$            event.getPartialTick().getRealtimeDeltaTicks()
    //#else
    //$$            event.getPartialTick()
    //#endif
    //$$     );
    //$$ }
    //#endif
    //$$
    //$$ @EventBusSubscriber(
    //$$         modid = "plasmovoice",
    //#if MC>=12106
    //$$         value = Dist.CLIENT
    //#else
    //$$         value = Dist.CLIENT,
    //$$         bus = EventBusSubscriber.Bus.MOD
    //#endif
    //$$ )
    //$$ public static class ModBusEvents {
    //$$
    //$$     @SubscribeEvent
    //$$     public static void onKeyMappingsRegister(RegisterKeyMappingsEvent event) {
    //$$         event.register(MENU_KEY);
    //$$     }
    //$$ }

    //#endif
}

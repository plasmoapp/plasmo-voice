package su.plo.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import su.plo.slib.api.logging.McLoggerFactory;
import su.plo.slib.mod.logging.Log4jLogger;
import su.plo.voice.client.gui.settings.VoiceScreens;
import su.plo.voice.universal.UKeyboard;
import su.plo.voice.universal.UMinecraft;
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
import su.plo.voice.client.render.ModEntityRenderer;
import su.plo.voice.client.render.ModHudRenderer;
import su.plo.voice.client.render.ModLevelRenderer;
import su.plo.voice.util.version.ModrinthLoader;

//#if FABRIC
import su.plo.voice.server.ModVoiceServer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
//#else
//$$ import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent;
//$$
//$$ import net.minecraftforge.network.event.EventNetworkChannel;
//$$ import net.minecraftforge.client.event.RenderGuiOverlayEvent;
//$$
//#if MC>=11900
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import net.minecraftforge.api.distmarker.Dist;
//$$ import net.minecraftforge.client.event.RenderLevelStageEvent;
//$$ import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
//$$ import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
//#else
//$$ import net.minecraftforge.client.ClientRegistry;
//$$ import net.minecraftforge.client.event.RenderLevelLastEvent;
//#endif

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
            UKeyboard.KEY_V,
            "Plasmo Voice"
    );

    @Getter
    private final ModHudRenderer hudRenderer;
    @Getter
    private final ModLevelRenderer levelRenderer;
    @Getter
    private final ModEntityRenderer entityRenderer;

    private final ModClientChannelHandler handler = new ModClientChannelHandler(this);

    public ModVoiceClient() {
        //#if FORGE
        //$$ super(ModrinthLoader.FORGE);
        //#else
        super(ModrinthLoader.FABRIC);
        //#endif

        McLoggerFactory.supplier = Log4jLogger::new;

        DeviceFactoryManager factoryManager = getDeviceFactoryManager();

        // OpenAL
        factoryManager.registerDeviceFactory(new AlOutputDeviceFactory(this));
        factoryManager.registerDeviceFactory(new AlInputDeviceFactory(this));

        // JavaX input
        getDeviceFactoryManager().registerDeviceFactory(new JavaxInputDeviceFactory(this));

        this.hudRenderer = new ModHudRenderer(this);
        this.levelRenderer = new ModLevelRenderer(this);
        this.entityRenderer = new ModEntityRenderer(this);

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
        if (UMinecraft.getPlayer() == null) return;
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
        WorldRenderEvents.END.register(
                (context) -> levelRenderer.render(context.world(), context.matrixStack(), context.camera(), context.tickDelta())
        );
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onServerDisconnect());
        ClientPlayNetworking.registerGlobalReceiver(ModVoiceServer.CHANNEL, handler);
        ClientPlayNetworking.registerGlobalReceiver(ModVoiceServer.FLAG_CHANNEL, (client, handler, buf, responseSender) -> {});

        KeyBindingHelper.registerKeyBinding(MENU_KEY);
    }

    //#else

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
    //$$             UMinecraft.getWorld() == null
    //$$     ) return;
    //$$     levelRenderer.render(UMinecraft.getWorld(), event.getPoseStack(), event.getCamera(), event.getPartialTick());
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
    //$$             UMinecraft.getWorld(),
    //$$             event.getPoseStack(),
    //$$             UMinecraft.getMinecraft().gameRenderer.getMainCamera(),
    //$$             event.getPartialTick()
    //$$     );
    //$$ }
    //#endif

    //#endif
}

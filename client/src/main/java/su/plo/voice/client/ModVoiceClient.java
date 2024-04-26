package su.plo.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import gg.essential.universal.UKeyboard;
import gg.essential.universal.UMinecraft;
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
import su.plo.voice.server.ModVoiceServer;
import su.plo.voice.util.version.ModrinthLoader;

//#if FABRIC
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

//#if MC>=12005
//$$ import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//$$ import su.plo.voice.codec.PacketFlagTcpPayload;
//$$ import su.plo.voice.codec.PacketFlagTcpPayloadCodec;
//$$ import su.plo.voice.codec.PacketTcpPayload;
//$$ import su.plo.voice.codec.PacketTcpPayloadCodec;
//#endif

//#else
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import net.minecraftforge.api.distmarker.Dist;
//$$ import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
//$$ import net.minecraftforge.client.event.RenderGuiOverlayEvent;
//$$ import net.minecraftforge.client.event.RenderLevelStageEvent;
//$$ import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent;
//$$ import net.minecraftforge.fml.ModList;
//$$ import net.minecraftforge.network.event.EventNetworkChannel;
//$$ import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
//#endif

import java.io.File;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

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
        if (MENU_KEY.consumeClick()) openSettings();
    }

    @Override
    public @NotNull File getConfigFolder() {
        return new File("config/" + modId);
    }

    @Override
    public @NotNull File getConfigsFolder() {
        return new File("config");
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
        WorldRenderEvents.LAST.register(
                (context) -> levelRenderer.render(context.world(), context.matrixStack(), context.camera(), context.tickDelta())
        );
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onServerDisconnect());

        //#if MC>=12005
        //$$ PayloadTypeRegistry.playS2C().register(
        //$$         PacketTcpPayload.TYPE,
        //$$         new PacketTcpPayloadCodec()
        //$$ );
        //$$ ClientPlayNetworking.registerGlobalReceiver(PacketTcpPayload.TYPE, handler);
        //$$
        //$$ PayloadTypeRegistry.playS2C().register(
        //$$         PacketFlagTcpPayload.TYPE,
        //$$         new PacketFlagTcpPayloadCodec()
        //$$ );
        //$$ ClientPlayNetworking.registerGlobalReceiver(PacketFlagTcpPayload.TYPE, (payload, context) -> {});
        //#else
        ClientPlayNetworking.registerGlobalReceiver(ModVoiceServer.CHANNEL, handler);
        ClientPlayNetworking.registerGlobalReceiver(ModVoiceServer.FLAG_CHANNEL, (client, handler, buf, responseSender) -> {});
        //#endif

        KeyBindingHelper.registerKeyBinding(MENU_KEY);
    }

    @Override
    public @NotNull String getVersion() {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer(modId)
                .orElse(null);
        checkNotNull(modContainer, "modContainer cannot be null");
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }
    //#else
    //$$ public void onInitialize(EventNetworkChannel channel) {
    //$$     channel.addListener(handler::receive);
    //$$     super.onInitialize();
    //$$ }
    //$$ // todo: onShutdown mixin?
    //$$ @SubscribeEvent
    //$$ public void onOverlayRender(RenderGuiOverlayEvent.Post event) {
    //$$     if (!event.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id())) return;
             //#if MC>=12000
             //$$ hudRenderer.render(event.getGuiGraphics(), event.getPartialTick());
             //#else
             //$$ hudRenderer.render(event.getPoseStack(), event.getPartialTick());
             //#endif
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onWorldRender(RenderLevelStageEvent event) {
    //$$     if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES ||
    //$$             UMinecraft.getWorld() == null
    //$$     ) return;
    //$$     levelRenderer.render(UMinecraft.getWorld(), event.getPoseStack(), event.getCamera(), event.getPartialTick());
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
    //$$     onServerDisconnect();
    //$$ }
    //$$
    //$$ @Override
    //$$ public @NotNull String getVersion() {
    //$$     return ModList.get().getModFileById("plasmovoice").versionString();
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
    //#endif
}

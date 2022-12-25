package su.plo.voice.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.forge.client.connection.ForgeClientChannelHandler;
import su.plo.voice.mod.client.ModVoiceClient;

public final class ForgeVoiceClient extends ModVoiceClient<ForgeClientChannelHandler> {

    private EventNetworkChannel channel;

    public ForgeVoiceClient() {
    }

    public void onInitialize(EventNetworkChannel channel) {
        this.channel = channel;
        super.onInitialize();
    }

    // todo: onShutdown mixin?
    @SubscribeEvent
    public void onOverlayRender(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id())) return;
        hudRenderer.render(event.getPoseStack(), event.getPartialTick());
    }

    @SubscribeEvent
    public void onWorldRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS ||
                Minecraft.getInstance().level == null
        ) return;
        levelRenderer.render(Minecraft.getInstance().level, event.getPoseStack(), event.getCamera(), event.getPartialTick());
    }

    @SubscribeEvent
    public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        onServerDisconnect();
    }

    @SubscribeEvent
    public void onKeyMappingsRegister(RegisterKeyMappingsEvent event) {
        event.register(menuKey);
    }

    @Override
    public @NotNull String getVersion() {
        return ModList.get().getModFileById("plasmovoice").versionString();
    }

    @Override
    protected ForgeClientChannelHandler createChannelHandler() {
        return new ForgeClientChannelHandler(this, minecraftLib, channel);
    }
}

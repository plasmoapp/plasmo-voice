package su.plo.voice.client.event;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.client.gui.VoiceHud;

public class RenderEvent {
    private final VoiceHud voiceHud = new VoiceHud();

    @SubscribeEvent
    public void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id())) return;
        this.voiceHud.render();
    }
}

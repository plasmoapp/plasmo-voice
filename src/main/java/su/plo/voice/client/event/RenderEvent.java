package su.plo.voice.client.event;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import su.plo.voice.client.gui.VoiceHud;

public class RenderEvent {
    private final VoiceHud voiceHud = new VoiceHud();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (!event.getType().equals(RenderGameOverlayEvent.ElementType.CHAT)) {
            return;
        }

        this.voiceHud.render();
    }
}

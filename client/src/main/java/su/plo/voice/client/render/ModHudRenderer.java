package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.event.render.HudRenderEvent;

//#if MC>=12000
//$$ import net.minecraft.client.gui.GuiGraphics;
//#else

//#endif

public final class ModHudRenderer extends ModRenderer {

    public ModHudRenderer(@NotNull PlasmoVoiceClient voiceClient) {
        super(voiceClient);
    }

    //#if MC>=12000
    //$$ public void render(@NotNull GuiGraphics graphics, float delta) {
    //$$     voiceClient.getEventBus().fire(new HudRenderEvent(graphics.pose(), delta));
    //$$ }
    //#else
    public void render(@NotNull PoseStack poseStack, float delta) {
        voiceClient.getEventBus().fire(new HudRenderEvent(poseStack, delta));
    }
    //#endif
}

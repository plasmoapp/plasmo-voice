package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.essential.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.event.render.HudRenderEvent;

//#if MC>=12100
//$$ import net.minecraft.client.DeltaTracker;
//#endif

//#if MC>=12000
//$$ import net.minecraft.client.gui.GuiGraphics;
//#else
import com.mojang.blaze3d.vertex.PoseStack;
//#endif

public final class ModHudRenderer extends ModRenderer {

    public ModHudRenderer(@NotNull PlasmoVoiceClient voiceClient) {
        super(voiceClient);
    }

    //#if MC>=12100
    //$$ public void render(@NotNull GuiGraphics graphics, DeltaTracker delta) {
    //$$     voiceClient.getEventBus().call(new HudRenderEvent(new UMatrixStack(graphics.pose()), delta.getRealtimeDeltaTicks()));
    //$$ }
    //#elseif MC>=12000
    //$$ public void render(@NotNull GuiGraphics graphics, float delta) {
    //$$     voiceClient.getEventBus().call(new HudRenderEvent(new UMatrixStack(graphics.pose()), delta));
    //$$ }
    //#else
    public void render(@NotNull PoseStack poseStack, float delta) {
        voiceClient.getEventBus().call(new HudRenderEvent(new UMatrixStack(poseStack), delta));
    }
    //#endif
}

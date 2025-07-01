package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.voice.client.event.HudRenderEvent;

//#if MC>=12100
//$$ import net.minecraft.client.DeltaTracker;
//#endif

//#if MC>=12000
//$$ import net.minecraft.client.gui.GuiGraphics;
//#endif

public final class ModHudRenderer {

    //#if MC>=12100
    //$$ public void render(@NotNull GuiGraphics graphics, DeltaTracker delta) {
    //$$     render(graphics, delta.getRealtimeDeltaTicks());
    //$$ }
    //$$
    //$$ public void render(@NotNull GuiGraphics graphics, float delta) {
    //$$     HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(graphics), delta);
    //$$ }
    //#elseif MC>=12000
    //$$ public void render(@NotNull GuiGraphics graphics, float delta) {
    //$$     HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(graphics), delta);
    //$$ }
    //#else
    public void render(@NotNull PoseStack poseStack, float delta) {
        HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(poseStack), delta);
    }
    //#endif
}

package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.voice.client.event.HudRenderEvent;

//#if MC>=1.21.6 && FABRIC
//$$ import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
//$$ import net.minecraft.resources.ResourceLocation;
//$$ import su.plo.lib.mod.client.ResourceLocationUtil;
//#endif

//#if MC>=12100
//$$ import net.minecraft.client.DeltaTracker;
//#endif

//#if MC>=12000
//$$ import net.minecraft.client.gui.GuiGraphics;
//#endif

//#if MC>=1.21.6 && FABRIC
//$$ public final class ModHudRenderer implements HudElement {
//$$     public static ResourceLocation KEY = ResourceLocationUtil.mod("hud");
//$$
//$$     @Override
//$$     public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
//$$         HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(graphics), deltaTracker.getRealtimeDeltaTicks());
//$$     }
//$$ }
//#else
public final class ModHudRenderer {

    //#if MC>=12100
    //$$ public static void render(@NotNull GuiGraphics graphics, DeltaTracker delta) {
    //$$     render(graphics, delta.getRealtimeDeltaTicks());
    //$$ }
    //$$
    //$$ public static void render(@NotNull GuiGraphics graphics, float delta) {
    //$$     HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(graphics), delta);
    //$$ }
    //#elseif MC>=12000
    //$$ public static void render(@NotNull GuiGraphics graphics, float delta) {
    //$$     HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(graphics), delta);
    //$$ }
    //#else
    public static void render(@NotNull PoseStack poseStack, float delta) {
        HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(poseStack), delta);
    }
    //#endif
}
//#endif

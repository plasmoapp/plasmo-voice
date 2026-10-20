package su.plo.voice.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.ResourceLocationUtil;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.voice.client.event.HudRenderEvent;

//#if MC<26.4
import net.minecraft.client.DeltaTracker;
//#endif

public final class ModHudRenderer {
    public static final ResourceLocation KEY = ResourceLocationUtil.mod("hud");

    //#if MC>=26.4
    //$$ public static void render(@NotNull GuiGraphicsExtractor graphics) {
    //$$     HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(graphics), graphics.deltaTracker().getRealtimeDeltaTicks());
    //$$ }
    //#else
    public static void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(graphics), delta.getRealtimeDeltaTicks());
    }
    //#endif
}

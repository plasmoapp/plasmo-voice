package su.plo.voice.client.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.ResourceLocationUtil;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.voice.client.event.HudRenderEvent;

public final class ModHudRenderer {
    public static final ResourceLocation KEY = ResourceLocationUtil.mod("hud");

    public static void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker delta) {
        HudRenderEvent.INSTANCE.getInvoker().onRender(new GuiRenderContext(graphics), delta.getRealtimeDeltaTicks());
    }
}

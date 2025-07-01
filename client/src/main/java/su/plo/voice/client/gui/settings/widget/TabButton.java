package su.plo.voice.client.gui.settings.widget;

import su.plo.lib.mod.client.ResourceLocationUtil;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.lib.mod.client.render.pipeline.RenderPipelines;
import su.plo.lib.mod.client.render.shader.SolidColorShader;
import su.plo.slib.api.chat.component.McTextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.components.Button;

import java.awt.Color;

//#if MC>=11701

//#else
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//#endif

public final class TabButton extends Button {

    private final boolean shadow;
    private final Color shadowColor;

    private final ResourceLocation iconLocation;
    private final ResourceLocation disabledIconLocation;

    public TabButton(
            int x,
            int y,
            int width,
            int height,
            @NotNull McTextComponent text,
            @NotNull ResourceLocation iconLocation,
            @NotNull OnPress pressAction,
            @NotNull OnTooltip tooltipAction,
            boolean shadow
    ) {
        this(x, y, width, height, text, iconLocation, pressAction, tooltipAction, shadow, Colors.WHITE);
    }

    public TabButton(int x,
                     int y,
                     int width,
                     int height,
                     @NotNull McTextComponent text,
                     @NotNull ResourceLocation iconLocation,
                     @NotNull OnPress pressAction,
                     @NotNull OnTooltip tooltipAction,
                     boolean shadow,
                     Color shadowColor) {
        super(x, y, width, height, text, pressAction, tooltipAction);

        this.shadow = shadow;
        this.shadowColor = shadowColor;

        this.iconLocation = iconLocation;
        this.disabledIconLocation = ResourceLocationUtil.tryBuild(
                iconLocation.getNamespace(),
                iconLocation.getPath().replace(".png", "_disabled.png")
        );
    }

    @Override
    protected void renderText(@NotNull GuiRenderContext context, int mouseX, int mouseY) {
        if (shadow && SolidColorShader.isAvailable()) {
            Color shadowColor = active ? this.shadowColor : Colors.GRAY;

            context.blitColor(
                    getIconLocation(),
                    x + 7,
                    y + 7,
                    0.0F,
                    0.0F,
                    8,
                    8,
                    8,
                    8,
                    Colors.times(shadowColor, 0.25),
                    RenderPipelines.GUI_TEXTURE_SOLID_COLOR
            );
        }

        context.blit(getIconLocation(), x + 6, y + 6, 0, 0, 8, 8, 8, 8);

        Color textColor = active ? Colors.WHITE : Colors.GRAY;
        context.drawString(
                getText(),
                x + 16,
                y + (height - 8) / 2,
                Colors.withAlpha(textColor, alpha),
                true
        );
    }

    private ResourceLocation getIconLocation() {
        return active ? iconLocation : disabledIconLocation;
    }
}

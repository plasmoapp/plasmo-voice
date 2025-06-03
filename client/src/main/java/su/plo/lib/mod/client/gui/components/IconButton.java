package su.plo.lib.mod.client.gui.components;

import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.lib.mod.client.render.pipeline.RenderPipelines;
import su.plo.slib.api.chat.component.McTextComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.render.shader.SolidColorShader;

import java.awt.Color;

//#if MC<11701
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//#endif

public final class IconButton extends Button {

    private final boolean shadow;
    private final Color shadowColor;
    @Getter
    @Setter
    private ResourceLocation iconLocation;
    @Getter
    @Setter
    private @Nullable Color iconColor = null;

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            @NotNull OnPress pressAction,
            @NotNull OnTooltip tooltipAction,
            @NotNull ResourceLocation iconLocation,
            boolean shadow
    ) {
        this(x, y, width, height, pressAction, tooltipAction, iconLocation, shadow, Colors.WHITE);
    }

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            @NotNull OnPress pressAction,
            @NotNull OnTooltip tooltipAction,
            @NotNull ResourceLocation iconLocation,
            boolean shadow,
            Color shadowColor
    ) {
        super(x, y, width, height, McTextComponent.empty(), pressAction, tooltipAction);

        this.iconLocation = iconLocation;
        this.shadow = shadow;
        this.shadowColor = shadowColor;
    }

    @Override
    public void renderButton(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        super.renderButton(context, mouseX, mouseY, delta);

        if (hasShadow() && SolidColorShader.isAvailable()) {
            Color shadowColor = active ? this.shadowColor : Colors.GRAY;

            context.blitColor(
                    iconLocation,
                    x + 2,
                    y + 3,
                    0.0F,
                    0.0F,
                    16,
                    16,
                    16,
                    16,
                    Colors.times(shadowColor, 0.25),
                    RenderPipelines.GUI_TEXTURE_SOLID_COLOR
            );
        }

        Color iconColor = this.iconColor;
        if (iconColor == null && !active) {
            iconColor = Colors.GRAY;
        }

        if (iconColor != null) {
            context.blitColor(
                    iconLocation,
                    x + 2,
                    y + 2,
                    0.0F,
                    0.0F,
                    16,
                    16,
                    16,
                    16,
                    iconColor
            );
        } else {
            context.blit(iconLocation, x + 2, y + 2, 0, 0, 16, 16, 16, 16);
        }
    }

    public boolean hasShadow() {
        return shadow;
    }
}

package su.plo.voice.client.gui.settings.widget;

import su.plo.voice.universal.UGraphics;
import su.plo.voice.universal.UMatrixStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.shader.SolidColorShader;

public final class TabButton extends Button {

    private final boolean shadow;
    private final int shadowColor;

    private final ResourceLocation iconLocation;
    private final ResourceLocation disabledIconLocation;

    public TabButton(int x,
                     int y,
                     int width,
                     int height,
                     @NotNull MinecraftTextComponent text,
                     @NotNull ResourceLocation iconLocation,
                     @NotNull OnPress pressAction,
                     @NotNull OnTooltip tooltipAction,
                     boolean shadow) {
        this(x, y, width, height, text, iconLocation, pressAction, tooltipAction, shadow, -0x1);
    }

    public TabButton(int x,
                     int y,
                     int width,
                     int height,
                     @NotNull MinecraftTextComponent text,
                     @NotNull ResourceLocation iconLocation,
                     @NotNull OnPress pressAction,
                     @NotNull OnTooltip tooltipAction,
                     boolean shadow,
                     int shadowColor) {
        super(x, y, width, height, text, pressAction, tooltipAction);

        this.shadow = shadow;
        this.shadowColor = shadowColor;

        this.iconLocation = iconLocation;
        this.disabledIconLocation = new ResourceLocation(
                iconLocation.getNamespace(),
                iconLocation.getPath().replace(".png", "_disabled.png")
        );
    }

    @Override
    protected void renderText(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        UGraphics.bindTexture(0, getIconLocation());

        if (shadow) {
            int shadowColor = active ? this.shadowColor : -6250336;

            SolidColorShader.bind();

            RenderUtil.blitWithActiveShader(
                    stack,
                    UGraphics.CommonVertexFormats.POSITION_TEXTURE_COLOR,
                    x + 7,
                    x + 7 + 8,
                    y + 7,
                    y + 7 + 8,
                    0,
                    0, 1F,
                    0, 1F,
                    (int) ((shadowColor >> 16 & 255) * 0.25),
                    (int) ((shadowColor >> 8 & 255) * 0.25),
                    (int) ((shadowColor & 255) * 0.25),
                    shadowColor >> 24 & 255
            );

            SolidColorShader.unbind();
        }

        RenderUtil.blit(stack, x + 6, y + 6, 0, 0, 8, 8, 8, 8);

        int textColor = active ? COLOR_WHITE : COLOR_GRAY;
        RenderUtil.drawString(
                stack,
                getText(),
                x + 16,
                y + (height - 8) / 2,
                textColor | ((int) Math.ceil(this.alpha * 255.0F)) << 24
        );
    }

    private ResourceLocation getIconLocation() {
        return active ? iconLocation : disabledIconLocation;
    }
}

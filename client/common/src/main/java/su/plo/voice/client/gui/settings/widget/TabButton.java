package su.plo.voice.client.gui.settings.widget;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.components.Button;
import su.plo.lib.api.client.render.VertexBuilder;

public final class TabButton extends Button {

    private final boolean shadow;
    private final int shadowColor;

    private final String iconLocation;
    private final String disabledIconLocation;

    public TabButton(@NotNull MinecraftClientLib minecraft,
                     int x,
                     int y,
                     int width,
                     int height,
                     @NotNull TextComponent text,
                     @NotNull String iconLocation,
                     @NotNull OnPress pressAction,
                     @NotNull OnTooltip tooltipAction,
                     boolean shadow) {
        this(minecraft, x, y, width, height, text, iconLocation, pressAction, tooltipAction, shadow, -0x1);
    }

    public TabButton(@NotNull MinecraftClientLib minecraft,
                     int x,
                     int y,
                     int width,
                     int height,
                     @NotNull TextComponent text,
                     @NotNull String iconLocation,
                     @NotNull OnPress pressAction,
                     @NotNull OnTooltip tooltipAction,
                     boolean shadow,
                     int shadowColor) {
        super(minecraft, x, y, width, height, text, pressAction, tooltipAction);

        this.shadow = shadow;
        this.shadowColor = shadowColor;

        this.iconLocation = iconLocation;
        this.disabledIconLocation = iconLocation.replace(".png", "_disabled.png");
    }

    @Override
    protected void renderText(@NotNull GuiRender render, int mouseX, int mouseY) {
        render.setShaderTexture(0, getIconLocation());

        if (shadow) {
            int shadowColor = active ? this.shadowColor : -6250336;

            render.blitColorShader(
                    VertexBuilder.Shader.POSITION_TEX_SOLID_COLOR,
                    VertexBuilder.Format.POSITION_TEX_SOLID_COLOR,
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
        }

        render.blit(x + 6, y + 6, 0, 0, 8, 8, 8, 8);

        int textColor = active ? COLOR_WHITE : COLOR_GRAY;
        render.drawString(
                getText(),
                x + 16,
                y + (height - 8) / 2,
                textColor | ((int) Math.ceil(this.alpha * 255.0F)) << 24
        );
    }

    private String getIconLocation() {
        return active ? iconLocation : disabledIconLocation;
    }
}

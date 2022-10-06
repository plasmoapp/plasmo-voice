package su.plo.lib.client.gui.components;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.render.VertexBuilder;

public final class IconButton extends Button {

    private final boolean shadow;
    private final int shadowColor;
    @Getter
    @Setter
    private String iconLocation;
    @Getter
    @Setter
    private int iconColor;

    public IconButton(@NotNull MinecraftClientLib minecraft,
                      int x,
                      int y,
                      int width,
                      int height,
                      @NotNull OnPress pressAction,
                      @NotNull OnTooltip tooltipAction,
                      @NotNull String iconLocation,
                      boolean shadow) {
        this(minecraft, x, y, width, height, pressAction, tooltipAction, iconLocation, shadow, -0x1);
    }

    public IconButton(@NotNull MinecraftClientLib minecraft,
                      int x,
                      int y,
                      int width,
                      int height,
                      @NotNull OnPress pressAction,
                      @NotNull OnTooltip tooltipAction,
                      @NotNull String iconLocation,
                      boolean shadow,
                      int shadowColor) {
        super(minecraft, x, y, width, height, TextComponent.empty(), pressAction, tooltipAction);

        this.iconLocation = iconLocation;
        this.shadow = shadow;
        this.shadowColor = shadowColor;
    }

    @Override
    public void renderButton(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        super.renderButton(render, mouseX, mouseY, delta);

        render.setShaderTexture(0, iconLocation);
        render.enableDepthTest();

        if (hasShadow()) {
            int shadowColor = active ? this.shadowColor : -6250336;

            render.blitColorShader(
                    VertexBuilder.Shader.POSITION_TEX_SOLID_COLOR,
                    VertexBuilder.Format.POSITION_TEX_SOLID_COLOR,
                    x + 2,
                    x + 2 + 16,
                    y + 3,
                    y + 3 + 16,
                    0,
                    0, 1F,
                    0, 1F,
                    (int) ((shadowColor >> 16 & 255) * 0.25),
                    (int) ((shadowColor >> 8 & 255) * 0.25),
                    (int) ((shadowColor & 255) * 0.25),
                    shadowColor >> 24 & 255
            );
        }

        if (iconColor != 0) {
            float alpha = (float) (iconColor >> 24 & 255) / 255F;
            float red = (float) (iconColor >> 16 & 255) / 255F;
            float green = (float) (iconColor >> 8 & 255) / 255F;
            float blue = (float) (iconColor & 255) / 255F;

            render.setShaderColor(red, green, blue, alpha);
        } else {
            render.setShaderColor(1F, 1F, 1F, 1F);
        }
        render.blit(x + 2, y + 2, 0, 0, 16, 16, 16, 16);

        render.disableDepthTest();
    }

    public boolean hasShadow() {
        return shadow;
    }
}

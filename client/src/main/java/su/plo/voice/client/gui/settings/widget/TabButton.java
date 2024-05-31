package su.plo.voice.client.gui.settings.widget;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import su.plo.slib.api.chat.component.McTextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.shader.SolidColorShader;

//#if MC>=11701
import com.mojang.blaze3d.systems.RenderSystem;
//#else
//$$ import com.mojang.blaze3d.platform.GlStateManager;
//#endif

public final class TabButton extends Button {

    private final boolean shadow;
    private final int shadowColor;

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
        this(x, y, width, height, text, iconLocation, pressAction, tooltipAction, shadow, -0x1);
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
                     int shadowColor) {
        super(x, y, width, height, text, pressAction, tooltipAction);

        this.shadow = shadow;
        this.shadowColor = shadowColor;

        this.iconLocation = iconLocation;
        this.disabledIconLocation = ResourceLocation.tryBuild(
                iconLocation.getNamespace(),
                iconLocation.getPath().replace(".png", "_disabled.png")
        );
    }

    @Override
    protected void renderText(@NotNull PoseStack stack, int mouseX, int mouseY) {
        RenderUtil.bindTexture(0, iconLocation);

        if (shadow) {
            //#if MC>=11701
            int textureId = RenderSystem.getShaderTexture(0);
            //#else
            //$$ int textureId = GlStateManager.getActiveTextureName();
            //#endif

            int shadowColor = active ? this.shadowColor : -6250336;

            SolidColorShader.bind(textureId);

            RenderUtil.blitWithActiveShader(
                    stack,
                    DefaultVertexFormat.POSITION_TEX_COLOR,
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

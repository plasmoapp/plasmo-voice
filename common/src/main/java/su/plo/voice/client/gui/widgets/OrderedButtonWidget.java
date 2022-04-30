package su.plo.voice.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class OrderedButtonWidget extends Button {
    private final Minecraft client = Minecraft.getInstance();
    private FormattedCharSequence orderedText;

    public OrderedButtonWidget(int x, int y, int width, int height, FormattedCharSequence message, OnPress onPress) {
        super(x, y, width, height, new TextComponent("Pepega"), onPress);
        this.orderedText = message;
    }

    public OrderedButtonWidget(int x, int y, int width, int height, FormattedCharSequence message, OnPress onPress, OnTooltip tooltipSupplier) {
        super(x, y, width, height, TextComponent.EMPTY, onPress, tooltipSupplier);
        this.orderedText = message;
    }

    public void setMessage(FormattedCharSequence message) {
        this.orderedText = message;
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Font textRenderer = client.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        blit(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(matrices, client, mouseX, mouseY);
        int j = this.active ? 16777215 : 10526880;

        textRenderer.drawShadow(matrices, orderedText, (float)((this.x + this.width / 2) - textRenderer.width(orderedText) / 2), this.y + (this.height - 8) / 2,
                j | Mth.ceil(this.alpha * 255.0F) << 24);

        if (this.isHovered()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }
}

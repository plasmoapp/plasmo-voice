package su.plo.lib.mod.client.gui.components;

import gg.essential.universal.UGraphics;
import gg.essential.universal.UKeyboard;
import gg.essential.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.MathLib;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.RenderUtil;

public abstract class AbstractSlider extends GuiAbstractWidget {

    protected double value;
    protected boolean dragging;

    public AbstractSlider(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected int getYImage(boolean hovered) {
        return 0;
    }

    @Override
    public void renderButton(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack, mouseX, mouseY);
        renderTrack(stack, mouseX, mouseY);
        renderText(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBackground(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        int width = getSliderWidth();

        UGraphics.bindTexture(0, WIDGETS_LOCATION);
        UGraphics.color4f(1F, 1F, 1F, alpha);

        UGraphics.enableBlend();
        RenderUtil.defaultBlendFunc();
        UGraphics.enableDepth();

        int textureV = getYImage(hovered);

        RenderUtil.blit(stack, x, y, 0, 46 + textureV * 20, width / 2, height);
        RenderUtil.blit(stack, x + width / 2, y, 200 - width / 2, 46 + textureV * 20, width / 2, height);
    }

    @Override
    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y &&
                mouseX < x + getSliderWidth() - 1 &&
                mouseY < y + height;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isHovered(mouseX, mouseY)) {
            this.dragging = true;
            setValueFromMouse(mouseX);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, @Nullable UKeyboard.Modifiers modifiers) {
        boolean rightPressed = keyCode == 263; // GLFW_KEY_RIGHT
        if (rightPressed || keyCode == 262) { // GLFW_KEY_LEFT
            float delta = rightPressed ? -1.0F : 1.0F;
            setValue(value + (double) (delta / (float) (getSliderWidth() - 8)));
        }

        return false;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (isHovered(mouseX, mouseY)) {
            setValueFromMouse(mouseX);
            this.dragging = true;
            super.onDrag(mouseX, mouseY, deltaX, deltaY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (dragging) {
            this.dragging = false;
            super.playDownSound();
        }
    }

    @Override
    protected void playDownSound() {
    }

    protected void renderTrack(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        UGraphics.bindTexture(0, WIDGETS_LOCATION);
        UGraphics.color4f(1F, 1F, 1F, 1F);
        int k = (isHoveredOrFocused() ? 2 : 1) * 20;
        RenderUtil.blit(stack, x + (int) (value * (double) (getSliderWidth() - 8)), y, 0, 46 + k, 4, 20);
        RenderUtil.blit(stack, x + (int) (value * (double) (getSliderWidth() - 8)) + 4, y, 196, 46 + k, 4, 20);
    }

    @Override
    protected void renderText(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        int textColor = active ? COLOR_WHITE : COLOR_GRAY;
        RenderUtil.drawCenteredString(
                stack,
                getText(),
                x + getSliderWidth() / 2,
                y + height / 2 - UGraphics.getFontHeight() / 2,
                textColor | ((int) Math.ceil(this.alpha * 255.0F)) << 24
        );
    }

    protected int getSliderWidth() {
        return width;
    }

    private void setValueFromMouse(double mouseX) {
        setValue((mouseX - (double) (x + 4)) / (double) (getSliderWidth() - 8));
    }

    private void setValue(double value) {
        double oldValue = this.value;
        this.value = MathLib.clamp(value, 0.0, 1.0);
        if (oldValue != this.value) applyValue();

        updateText();
    }

    protected abstract void updateText();

    protected abstract void applyValue();
}

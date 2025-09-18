package su.plo.lib.mod.client.gui.components;

import net.minecraft.util.Mth;
import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;

import java.awt.Color;

//#if MC>=12109
//$$ import com.mojang.blaze3d.platform.cursor.CursorTypes;
//#endif

public abstract class AbstractSlider extends GuiAbstractWidget {

    protected double value;
    protected boolean dragging;

    public AbstractSlider(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected @NotNull GuiWidgetTexture getButtonTexture(boolean hovered) {
        return GuiWidgetTexture.SLIDER;
    }

    @Override
    public void render(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        //#if MC>=12109
        //$$ if (isHovered()) {
        //$$     context.requestCursor(dragging ? CursorTypes.RESIZE_EW : CursorTypes.POINTING_HAND);
        //$$ }
        //#endif
    }

    @Override
    public void renderButton(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY);
        renderTrack(context, mouseX, mouseY);
        renderText(context, mouseX, mouseY);
    }

    @Override
    protected void renderBackground(@NotNull GuiRenderContext context, int mouseX, int mouseY) {
        int width = getSliderWidth();
        GuiWidgetTexture sprite = getButtonTexture(hovered);

        context.blitSprite(sprite, x, y, 0, 0, width / 2, height);
        context.blitSprite(sprite, x + width / 2, y, sprite.getSpriteWidth() - width / 2, 0, width / 2, height);
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
    public boolean keyPressed(int keyCode, int modifiers) {
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

    protected void renderTrack(@NotNull GuiRenderContext context, int mouseX, int mouseY) {
        int x0 = x + (int) (value * (double) (getSliderWidth() - 8));

        //#if MC>=12002
        //$$ GuiWidgetTexture sprite = isHoveredOrFocused() ? GuiWidgetTexture.SLIDER_HANDLE_ACTIVE : GuiWidgetTexture.SLIDER_HANDLE;
        //$$
        //$$ context.blitSprite(sprite, x0, y, 0, 0, 8, 20);
        //#else
        GuiWidgetTexture sprite;
        if (isHoveredOrFocused()) {
            sprite = GuiWidgetTexture.BUTTON_ACTIVE;
        } else {
            sprite = GuiWidgetTexture.BUTTON_DEFAULT;
        }

        context.blitSprite(sprite, x0, y, 0, 0, 4, 20);
        context.blitSprite(sprite, x0 + 4, y, sprite.getSpriteWidth() - 4, 0, 4, 20);
        //#endif
    }

    @Override
    protected void renderText(@NotNull GuiRenderContext context, int mouseX, int mouseY) {
        Color textColor = Colors.withAlpha(active ? Colors.WHITE : Colors.GRAY, alpha);
        context.drawCenteredString(
                getText(),
                x + getSliderWidth() / 2,
                y + height / 2 - RenderUtil.getFontHeight() / 2,
                textColor
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
        this.value = Mth.clamp(value, 0.0, 1.0);
        if (oldValue != this.value) applyValue();

        updateText();
    }

    protected abstract void updateText();

    protected abstract void applyValue();
}

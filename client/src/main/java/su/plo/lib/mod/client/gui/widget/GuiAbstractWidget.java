package su.plo.lib.mod.client.gui.widget;

import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.universal.UGraphics;
import su.plo.voice.universal.UMatrixStack;
import su.plo.voice.universal.USound;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.narration.NarrationOutput;
import su.plo.lib.mod.client.render.RenderUtil;

public abstract class GuiAbstractWidget implements GuiWidget, GuiNarrationWidget, GuiWidgetListener {

    public static McTextComponent wrapDefaultNarrationMessage(McTextComponent component) {
        return McTextComponent.translatable("gui.narrate.button", component);
    }

    public static int COLOR_WHITE = 0xFFFFFF;
    public static int COLOR_GRAY = 0xA0A0A0;

    @Getter
    @Setter
    protected int x;
    @Getter
    @Setter
    protected int y;

    @Getter
    @Setter
    protected int width;
    @Getter
    @Setter
    protected int height;

    @Setter
    protected McTextComponent text;
    protected float alpha = 1F;

    @Getter
    protected boolean hovered;
    @Getter
    @Setter
    protected boolean active = true;
    @Getter
    @Setter
    protected boolean visible = true;

    @Getter
    @Setter
    private boolean focused;

    public GuiAbstractWidget(int x,
                             int y,
                             int width,
                             int height) {
        this(x, y, width, height, McTextComponent.empty());
    }

    public GuiAbstractWidget(int x,
                             int y,
                             int width,
                             int height,
                             @NotNull McTextComponent text) {
        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.text = text;
    }

    // GuiWidget impl
    @Override
    public void render(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        this.hovered = isHovered(mouseX, mouseY);
        renderButton(stack, mouseX, mouseY, delta);
    }

    // GuiNarratableWidget impl
    @Override
    public NarrationPriority narrationPriority() {
        if (this.focused) {
            return NarrationPriority.FOCUSED;
        } else {
            return hovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }
    }

    @Override
    public void updateNarration(@NotNull NarrationOutput narrationOutput) {
        defaultButtonNarrationText(narrationOutput);
    }

    // GuiWidgetEventListener impl
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || !isValidClickButton(button)) return false;

        if (isClicked(mouseX, mouseY)) {
            playDownSound();
            onClick(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button)) {
            onRelease(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isValidClickButton(button)) {
            onDrag(mouseX, mouseY, deltaX, deltaY);
            return true;
        }

        return false;
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        if (!active || !visible) return false;

        this.focused = !lookForwards;
        onFocusedChanged(lookForwards);
        return lookForwards;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && visible && isHovered(mouseX, mouseY);
    }

    // Class methods
    public void renderButton(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        GuiWidgetTexture sprite = getButtonTexture(hovered);

        UGraphics.bindTexture(0, sprite.getLocation());
        UGraphics.color4f(1F, 1F, 1F, alpha);

        UGraphics.enableBlend();
        RenderUtil.defaultBlendFunc();
        UGraphics.enableDepth();

        RenderUtil.blitSprite(stack, sprite, x, y, 0, 0, width / 2, height);
        RenderUtil.blitSprite(stack, sprite, x + width / 2, y, sprite.getSpriteWidth() - width / 2, 0, width / 2, height);

        renderBackground(stack, mouseX, mouseY);

        renderText(stack, mouseX, mouseY);
    }

    public void renderToolTip(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
    }

    public void onClick(double mouseX, double mouseY) {
    }

    public void onRelease(double mouseX, double mouseY) {
    }

    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
    }

    public McTextComponent getText() {
        return text;
    }

    public boolean isHoveredOrFocused() {
        return this.hovered || this.focused;
    }

    protected void renderBackground(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
    }

    protected void renderText(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        int textColor = active ? COLOR_WHITE : COLOR_GRAY;
        RenderUtil.drawCenteredString(
                stack,
                getText(),
                x + width / 2,
                y + (height - 8) / 2,
                textColor | ((int) Math.ceil(this.alpha * 255.0F)) << 24
        );
    }

    protected void playDownSound() {
        USound.INSTANCE.playButtonPress();
    }

    protected void onFocusedChanged(boolean focused) {
    }

    protected void defaultButtonNarrationText(NarrationOutput narrationElementOutput) {
        narrationElementOutput.add(NarrationOutput.Type.TITLE, createNarrationMessage());
        if (active) {
            if (isFocused()) {
                narrationElementOutput.add(NarrationOutput.Type.USAGE, McTextComponent.translatable("narration.button.usage.focused"));
            } else {
                narrationElementOutput.add(NarrationOutput.Type.USAGE, McTextComponent.translatable("narration.button.usage.hovered"));
            }
        }
    }

    protected McTextComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(getText());
    }

    protected @NotNull GuiWidgetTexture getButtonTexture(boolean hovered) {
        if (!this.active) {
            return GuiWidgetTexture.BUTTON_DISABLED;
        } else if (hovered) {
            return GuiWidgetTexture.BUTTON_ACTIVE;
        }

        return GuiWidgetTexture.BUTTON_DEFAULT;
    }

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    protected boolean isClicked(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y &&
                mouseX < x + width &&
                mouseY < y + height;
    }
}

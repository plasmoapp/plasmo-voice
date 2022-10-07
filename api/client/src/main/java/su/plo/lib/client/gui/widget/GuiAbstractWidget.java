package su.plo.lib.client.gui.widget;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.narration.NarrationOutput;
import su.plo.lib.client.sound.MinecraftSoundManager;

public abstract class GuiAbstractWidget implements GuiWidget, GuiNarrationWidget, GuiWidgetListener {

    public static TextComponent wrapDefaultNarrationMessage(TextComponent component) {
        return TextComponent.translatable("gui.narrate.button", component);
    }

    public static int COLOR_WHITE = 0xFFFFFF;
    public static int COLOR_GRAY = 0xA0A0A0;

    protected final MinecraftClientLib minecraft;

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
    protected TextComponent text;
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

    public GuiAbstractWidget(@NotNull MinecraftClientLib minecraft,
                             int x,
                             int y,
                             int width,
                             int height) {
        this(minecraft, x, y, width, height, TextComponent.empty());
    }

    public GuiAbstractWidget(@NotNull MinecraftClientLib minecraft,
                             int x,
                             int y,
                             int width,
                             int height,
                             @NotNull TextComponent text) {
        this.minecraft = minecraft;

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.text = text;
    }

    // GuiWidget impl
    @Override
    public void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        this.hovered = isHovered(mouseX, mouseY);
        renderButton(render, mouseX, mouseY, delta);
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
    public void renderButton(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        render.setShaderTexture(0, WIDGETS_LOCATION);
        render.setShaderColor(1F, 1F, 1F, alpha);

        render.enableBlend();
        render.defaultBlendFunc();
        render.enableDepthTest();

        int textureV = getYImage(hovered);

        render.blit(x, y, 0, 46 + textureV * 20, width / 2, height);
        render.blit(x + width / 2, y, 200 - width / 2, 46 + textureV * 20, width / 2, height);

        renderBackground(render, mouseX, mouseY);

        renderText(render, mouseX, mouseY);
    }

    public void renderToolTip(@NotNull GuiRender render, int mouseX, int mouseY) {
    }

    public void onClick(double mouseX, double mouseY) {
    }

    public void onRelease(double mouseX, double mouseY) {
    }

    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
    }

    public TextComponent getText() {
        return text;
    }

    public boolean isHoveredOrFocused() {
        return this.hovered || this.focused;
    }

    protected void renderBackground(@NotNull GuiRender render, int mouseX, int mouseY) {
    }

    protected void renderText(@NotNull GuiRender render, int mouseX, int mouseY) {
        int textColor = active ? COLOR_WHITE : COLOR_GRAY;
        render.drawCenteredString(
                getText(),
                x + width / 2,
                y + (height - 8) / 2,
                textColor | ((int) Math.ceil(this.alpha * 255.0F)) << 24
        );
    }

    protected void playDownSound() {
        minecraft.getSoundManager().playSound(
                MinecraftSoundManager.Category.UI,
                "minecraft:ui.button.click",
                1F
        );
    }

    protected void onFocusedChanged(boolean focused) {
    }

    protected void defaultButtonNarrationText(NarrationOutput narrationElementOutput) {
        narrationElementOutput.add(NarrationOutput.Type.TITLE, createNarrationMessage());
        if (active) {
            if (isFocused()) {
                narrationElementOutput.add(NarrationOutput.Type.USAGE, TextComponent.translatable("narration.button.usage.focused"));
            } else {
                narrationElementOutput.add(NarrationOutput.Type.USAGE, TextComponent.translatable("narration.button.usage.hovered"));
            }
        }
    }

    protected TextComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(getText());
    }

    protected int getYImage(boolean hovered) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (hovered) {
            i = 2;
        }

        return i;
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

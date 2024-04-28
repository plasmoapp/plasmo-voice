package su.plo.voice.client.gui.settings.widget;

import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture;
import su.plo.slib.api.chat.component.McTextComponent;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.lib.mod.client.gui.components.AbstractSlider;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.client.config.hotkey.Hotkeys;

public final class VolumeSliderWidget extends AbstractSlider implements UpdatableWidget {

    private final Hotkeys keyBindings;
    private final DoubleConfigEntry entry;
    private final String suffix;

    public VolumeSliderWidget(
            @NotNull Hotkeys keyBindings,
            @NotNull DoubleConfigEntry entry,
            @NotNull String suffix,
            int x,
            int y,
            int width,
            int height
    ) {
        super(x, y, width, height);

        this.keyBindings = keyBindings;
        this.entry = entry;
        this.suffix = suffix;

        updateValue();
    }

    @Override
    protected void updateText() {
        this.text = McTextComponent.literal(Math.round(value * (entry.getMax() * 100D)) + suffix);
    }

    @Override
    protected void applyValue() {
        if (!keyBindings.getPressedKeys().contains(Hotkey.Type.KEYSYM.getOrCreate(340))) { // GLFW_KEY_LEFT_SHIFT
            this.value = calculateValue(value);
        }
        entry.set(value * entry.getMax());
    }

    @Override
    public void updateValue() {
        this.value = entry.value() / entry.getMax();
        updateText();
    }

    @Override
    protected void renderTrack(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        GuiWidgetTexture sprite;
        if (isHoveredOrFocused()) {
            sprite = GuiWidgetTexture.BUTTON_ACTIVE;
        } else {
            sprite = GuiWidgetTexture.BUTTON_DEFAULT;
        }

        UGraphics.bindTexture(0, sprite.getLocation());
        UGraphics.color4f(1F, 1F, 1F, 1F);

        int x0 = x + (int) (value * (double) (getSliderWidth() - 8));
        RenderUtil.blitSprite(stack, sprite, x0, y, 0, 0, 4, 20);
        RenderUtil.blitSprite(stack, sprite, x0 + 4, y, sprite.getSpriteWidth() - 4, 0, 4, 20);
    }

    private double calculateValue(double value) {
        return (Math.round((value * entry.getMax() * 100D) / 5) * 5) / (entry.getMax() * 100D);
    }
}

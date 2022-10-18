package su.plo.voice.client.gui.settings.widget;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.components.AbstractSlider;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.config.entry.DoubleConfigEntry;

public final class VolumeSliderWidget extends AbstractSlider implements UpdatableWidget {

    private final KeyBindings keyBindings;
    private final DoubleConfigEntry entry;
    private final String suffix;

    public VolumeSliderWidget(@NotNull MinecraftClientLib minecraft,
                              @NotNull KeyBindings keyBindings,
                              @NotNull DoubleConfigEntry entry,
                              @NotNull String suffix,
                              int x,
                              int y,
                              int width,
                              int height) {
        super(minecraft, x, y, width, height);

        this.keyBindings = keyBindings;
        this.entry = entry;
        this.suffix = suffix;

        updateValue();
    }

    @Override
    protected void updateText() {
        this.text = MinecraftTextComponent.literal((int) (value * (entry.getMax() * 100D)) + suffix);
    }

    @Override
    protected void applyValue() {
        if (!keyBindings.getPressedKeys().contains(KeyBinding.Type.KEYSYM.getOrCreate(340))) { // GLFW_KEY_LEFT_SHIFT
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
    protected void renderTrack(@NotNull GuiRender render, int mouseX, int mouseY) {
        render.setShaderTexture(0, WIDGETS_LOCATION);
        render.setShaderColor(1F, 1F, 1F, 1F);
        int k = (isHoveredOrFocused() ? 2 : 1) * 20;

        render.blit(x + (int) (value * (double) (getSliderWidth() - 8)), y, 0, 46 + k, 4, 20);
        render.blit(x + (int) (value * (double) (getSliderWidth() - 8)) + 4, y, 196, 46 + k, 4, 20);
    }

    private double calculateValue(double value) {
        return (Math.round((value * entry.getMax() * 100D) / 5) * 5) / (entry.getMax() * 100D);
    }
}

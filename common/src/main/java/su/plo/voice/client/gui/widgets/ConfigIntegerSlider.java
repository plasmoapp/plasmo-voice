package su.plo.voice.client.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import su.plo.voice.client.config.entries.IntegerConfigEntry;

public class ConfigIntegerSlider extends AbstractSliderButton {
    private final IntegerConfigEntry entry;
    private final Component suffix;
    private final UpdateAction onUpdate;

    public ConfigIntegerSlider(int x, int y, int width, Component suffix, IntegerConfigEntry entry, UpdateAction onUpdate) {
        super(x, y, width, 20, TextComponent.EMPTY, 0.0D);
        this.onUpdate = onUpdate;
        this.suffix = suffix;
        this.entry = entry;
        this.updateValue();
        this.updateMessage();
    }

    public ConfigIntegerSlider(int x, int y, int width, IntegerConfigEntry entry) {
        this(x, y, width, null, entry, null);
    }

    public void updateValue() {
        this.value = (double) (entry.get() - entry.getMin()) / (entry.getMax() - entry.getMin());
        if (this.onUpdate != null) {
            this.onUpdate.onUpdate(this.value);
        }
        this.updateMessage();
    }

    protected void updateMessage() {
        if (suffix != null) {
            this.setMessage(new TextComponent(String.valueOf((int) (this.value * (entry.getMax() - entry.getMin()) + entry.getMin())))
                    .append(new TextComponent(" "))
                    .append(suffix));
        } else {
            this.setMessage(new TextComponent(String.valueOf((int) (this.value * (entry.getMax() - entry.getMin()) + entry.getMin()))));
        }
    }

    public boolean isHovered() {
        return active && super.isHovered();
    }

    protected void applyValue() {
        entry.set((int) (this.value * (entry.getMax() - entry.getMin()) + entry.getMin()));
    }

    public interface UpdateAction {
        void onUpdate(double value);
    }
}
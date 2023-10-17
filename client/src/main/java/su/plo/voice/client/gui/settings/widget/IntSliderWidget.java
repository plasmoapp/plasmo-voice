package su.plo.voice.client.gui.settings.widget;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.mod.client.gui.components.AbstractSlider;
import su.plo.slib.api.chat.component.McTextComponent;

public final class IntSliderWidget extends AbstractSlider implements UpdatableWidget {

    private final IntConfigEntry entry;
    private final String suffix;

    public IntSliderWidget(
            @NotNull IntConfigEntry entry,
            @NotNull String suffix,
            int x,
            int y,
            int width,
            int height
    ) {
        super(x, y, width, height);

        this.entry = entry;
        this.suffix = suffix;

        updateValue();
    }

    @Override
    protected void updateText() {
        if (suffix != null) {
            setText(
                    McTextComponent.literal(String.valueOf(calculateValue(value)))
                            .append(McTextComponent.literal(" " + suffix))
            );
        } else {
            setText(McTextComponent.literal(String.valueOf(calculateValue(value))));
        }
    }

    @Override
    protected void applyValue() {
        entry.set(calculateValue(value));
    }

    @Override
    public void updateValue() {
        this.value = (double) (entry.value() - entry.getMin()) / (entry.getMax() - entry.getMin());
        updateText();
    }

    @Override
    public boolean isHoveredOrFocused() {
        return active && super.isHoveredOrFocused();
    }

    private int calculateValue(double value) {
        return (int) (value * (entry.getMax() - entry.getMin()) + entry.getMin());
    }
}

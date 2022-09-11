package su.plo.voice.client.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import su.plo.voice.config.entry.IntConfigEntry;

public final class IntSliderWidget extends AbstractSliderButton implements UpdatableWidget {

    private final IntConfigEntry entry;
    private final String suffix;

    public IntSliderWidget(int x, int y, int width, IntConfigEntry entry, String suffix) {
        super(x, y, width, 20, Component.empty(), 0.0D);

        this.suffix = suffix;
        this.entry = entry;

        updateValue();
        updateMessage();
    }

    @Override
    public void updateValue() {
        this.value = (double) (entry.value() - entry.getMin()) / (entry.getMax() - entry.getMin());
        updateMessage();
    }

    @Override
    public boolean isHoveredOrFocused() {
        return active && super.isHoveredOrFocused();
    }

    @Override
    protected void updateMessage() {
        if (suffix != null) {
            setMessage(
                    Component.literal(String.valueOf((int) (value * (entry.getMax() - entry.getMin()) + entry.getMin())))
                            .append(Component.literal(" "))
                            .append(suffix)
            );
        } else {
            setMessage(Component.literal(String.valueOf((int) (value * (entry.getMax() - entry.getMin()) + entry.getMin()))));
        }
    }

    @Override
    protected void applyValue() {
        entry.set((int) (value * (entry.getMax() - entry.getMin()) + entry.getMin()));
    }
}

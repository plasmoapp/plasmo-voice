package su.plo.voice.client.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import su.plo.voice.config.entry.DoubleConfigEntry;

public final class SliderWidget extends AbstractSliderButton implements UpdatableWidget {

    private final DoubleConfigEntry entry;
    private final String suffix;

    public SliderWidget(int x, int y, int width, DoubleConfigEntry entry, String suffix) {
        super(x, y, width, 20, Component.empty(), entry.value() / entry.getMax());

        this.entry = entry;
        this.suffix = suffix;

        this.updateMessage();
    }

    @Override
    public void updateValue() {
        this.value = entry.value() / entry.getMax();
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage(Component.literal((int) (value * (entry.getMax() * 100D)) + suffix));
    }

    protected void applyValue() {
        entry.set(value * entry.getMax());
    }
}

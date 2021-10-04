package su.plo.voice.client.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TextComponent;
import su.plo.voice.client.config.VoiceClientConfig;

public class ConfigIntegerSlider extends AbstractSliderButton {
    private final VoiceClientConfig.IntegerConfigEntry entry;

    public ConfigIntegerSlider(int x, int y, int width, VoiceClientConfig.IntegerConfigEntry entry) {
        super(x, y, width, 20, TextComponent.EMPTY, 0.0D);
        this.entry = entry;
        this.updateValue();
        this.updateMessage();
    }

    public void updateValue() {
        this.value = (double) (entry.get() - entry.getMin()) / (entry.getMax() - entry.getMin());
    }

    protected void updateMessage() {
        this.setMessage(new TextComponent(String.valueOf((int) (this.value * (entry.getMax() - entry.getMin()) + entry.getMin()))));
    }

    public boolean isHovered() {
        return active && super.isHovered();
    }

    protected void applyValue() {
        entry.set((int) (this.value * (entry.getMax() - entry.getMin()) + entry.getMin()));
    }
}

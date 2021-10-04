package su.plo.voice.client.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TextComponent;
import su.plo.voice.client.config.VoiceClientConfig;

public class VoiceVolumeSlider extends AbstractSliderButton {
    private final VoiceClientConfig.DoubleConfigEntry entry;

    public VoiceVolumeSlider(int x, int y, int width, VoiceClientConfig.DoubleConfigEntry entry) {
        super(x, y, width, 20, TextComponent.EMPTY, entry.get() / 2.0D);
        this.entry = entry;
        this.updateMessage();
    }

    public void updateValue() {
        this.value = entry.get() / 2.0D;
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage(new TextComponent((int)(this.value * 200.0D) + "%"));
    }

    protected void applyValue() {
        entry.set(this.value * 2.0D);
    }
}

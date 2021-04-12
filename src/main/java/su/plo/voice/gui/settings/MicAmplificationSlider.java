package su.plo.voice.gui.settings;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su.plo.voice.Voice;

public class MicAmplificationSlider extends AbstractSlider {
    private static final float MAXIMUM = 4F;

    public MicAmplificationSlider(int x, int y, int width) {
        super(x, y, width, 20, StringTextComponent.EMPTY, Voice.config.microphoneAmplification / MAXIMUM);
        this.updateMessage();
    }

    protected void updateMessage() {
        long amp = Math.round(this.value * MAXIMUM * 100F - 100F);
        this.setMessage((new TranslationTextComponent("gui.plasmo_voice.mic_amplification", (amp > 0F ? "+" : "") + amp + "%")));
    }

    protected void applyValue() {
        Voice.config.microphoneAmplification = this.value * MAXIMUM;
    }
}

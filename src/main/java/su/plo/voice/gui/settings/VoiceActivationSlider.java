package su.plo.voice.gui.settings;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su.plo.voice.Voice;
import su.plo.voice.utils.Utils;

public class VoiceActivationSlider extends AbstractSlider {
    public VoiceActivationSlider(int x, int y, int width) {
        super(x, y, width, 20, StringTextComponent.EMPTY, Utils.dbToPerc(Voice.config.voiceActivationThreshold));
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage((new TranslationTextComponent("gui.plasmo_voice.voice_activation", Math.round(Utils.percToDb(this.value)) + " dB")));
    }

    protected void applyValue() {
        Voice.config.voiceActivationThreshold = Utils.percToDb(this.value);
    }
}

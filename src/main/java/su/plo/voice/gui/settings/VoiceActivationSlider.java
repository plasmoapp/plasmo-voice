package su.plo.voice.gui.settings;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.utils.Utils;

public class VoiceActivationSlider extends SliderWidget {
    public VoiceActivationSlider(int x, int y, int width) {
        super(x, y, width, 20, LiteralText.EMPTY, Utils.dbToPerc(VoiceClient.getClientConfig().getVoiceActivationThreshold()));
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage((new TranslatableText("gui.plasmo_voice.voice_activation", Math.round(Utils.percToDb(this.value)) + " dB")));
    }

    protected void applyValue() {
        VoiceClient.getClientConfig().setVoiceActivationThreshold(Utils.percToDb(this.value));
    }
}

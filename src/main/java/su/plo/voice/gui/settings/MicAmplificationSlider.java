package su.plo.voice.gui.settings;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import su.plo.voice.client.VoiceClient;

public class MicAmplificationSlider extends SliderWidget {
    private static final float MAXIMUM = 4F;

    public MicAmplificationSlider(int x, int y, int width) {
        super(x, y, width, 20, LiteralText.EMPTY, VoiceClient.getClientConfig().getMicrophoneAmplification() / MAXIMUM);
        this.updateMessage();
    }

    protected void updateMessage() {
        long amp = Math.round(this.value * MAXIMUM * 100F - 100F);
        this.setMessage((new TranslatableText("gui.plasmo_voice.mic_amplification", (amp > 0F ? "+" : "") + amp + "%")));
    }

    protected void applyValue() {
        VoiceClient.getClientConfig().setMicrophoneAmplification(this.value * MAXIMUM);
    }
}

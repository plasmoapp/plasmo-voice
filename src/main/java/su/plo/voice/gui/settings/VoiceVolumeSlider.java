package su.plo.voice.gui.settings;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import su.plo.voice.client.VoiceClient;

public class VoiceVolumeSlider extends SliderWidget {
    public VoiceVolumeSlider(int x, int y, int width) {
        super(x, y, width, 20, LiteralText.EMPTY, VoiceClient.getClientConfig().getVoiceVolume() / 2.0D);
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage((new TranslatableText("gui.plasmo_voice.voice_chat_volume", (int)(this.value * 200.0D) + "%")));
    }

    protected void applyValue() {
        VoiceClient.getClientConfig().setVoiceVolume(this.value * 2.0D);
    }
}

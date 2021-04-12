package su.plo.voice.gui.settings;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su.plo.voice.Voice;

public class VoiceVolumeSlider extends AbstractSlider {
    public VoiceVolumeSlider(int x, int y, int width) {
        super(x, y, width, 20, StringTextComponent.EMPTY, Voice.config.voiceVolume / 2.0D);
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage((new TranslationTextComponent("gui.plasmo_voice.voice_chat_volume", (int)(this.value * 200.0D) + "%")));
    }

    protected void applyValue() {
        Voice.config.voiceVolume = this.value * 2.0D;
    }
}

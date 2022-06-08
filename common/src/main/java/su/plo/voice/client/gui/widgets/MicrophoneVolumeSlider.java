package su.plo.voice.client.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import su.plo.voice.client.VoiceClient;

public class MicrophoneVolumeSlider extends AbstractSliderButton {
    public MicrophoneVolumeSlider(int x, int y, int width) {
        super(x, y, width, 20, Component.empty(), VoiceClient.getClientConfig().microphoneAmplification.get() / 2.0D);
        this.updateMessage();
    }

    public void updateValue() {
        this.value = VoiceClient.getClientConfig().microphoneAmplification.get() / 2.0D;
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage(Component.literal((int) (this.value * 200.0D) + "%"));
    }

    protected void applyValue() {
        VoiceClient.getClientConfig().microphoneAmplification.set(this.value * 2.0D);
    }
}

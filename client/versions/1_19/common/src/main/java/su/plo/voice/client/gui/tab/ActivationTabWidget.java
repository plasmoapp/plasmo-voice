package su.plo.voice.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.client.gui.VoiceSettingsScreen;

public final class ActivationTabWidget extends TabWidget {

    private final AudioCapture capture;

    public ActivationTabWidget(Minecraft minecraft, VoiceSettingsScreen parent, AudioCapture capture) {
        super(minecraft, parent);

        this.capture = capture;
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(Component.translatable("gui.plasmovoice.devices.microphone")));
    }
}

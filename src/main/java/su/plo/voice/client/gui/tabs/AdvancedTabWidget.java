package su.plo.voice.client.gui.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widgets.ConfigIntegerSlider;
import su.plo.voice.client.gui.widgets.MicrophoneThresholdWidget;
import su.plo.voice.client.gui.widgets.ToggleButton;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.utils.TextUtils;
import su.plo.voice.rnnoise.Denoiser;

public class AdvancedTabWidget extends TabWidget {
    public AdvancedTabWidget(Minecraft client, VoiceSettingsScreen parent) {
        super(client, parent);

        VoiceClientConfig config = VoiceClient.getClientConfig();

        ToggleButton rnNoise = new ToggleButton(0, 0, 97, 20, config.rnNoise,
                toggled -> {
                    VoiceClient.recorder.toggleRnNoise();
                });
        rnNoise.active = Denoiser.platformSupported();

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.advanced.noise_reduction")));
        this.addEntry(new ConfigEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.rnnoise"),
                rnNoise,
                config.rnNoise,
                TextUtils.multiLine("gui.plasmo_voice.advanced.rnnoise.tooltip", 6),
                (button, element) -> {
                    VoiceClient.recorder.toggleRnNoise();
                    ((ToggleButton) element).updateValue();
                })
        );
        this.addEntry(new TabWidget.ConfigEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.microphone_testing"),
                new MicrophoneThresholdWidget(0, 0, 97, false, parent),
                null,
                null)
        );

        ConfigIntegerSlider directionalSourcesAngle = new ConfigIntegerSlider(0, 0, 97, config.directionalSourcesAngle);
        ToggleButton directionalSources = new ToggleButton(0, 0, 97, 20, config.directionalSources,
                toggled -> directionalSourcesAngle.active = toggled);
        directionalSourcesAngle.active = config.directionalSources.get();

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.advanced.engine")));
        this.addEntry(new ConfigEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.hrtf"),
                new ToggleButton(0, 0, 97, 20, config.hrtf,
                        toggled -> VoiceClient.getSoundEngine().toggleHRTF()),
                config.hrtf,
                TextUtils.multiLine("gui.plasmo_voice.advanced.hrtf.tooltip", 7),
                (button, element) -> {
                    VoiceClient.getSoundEngine().toggleHRTF();
                    ((ToggleButton) element).updateValue();
                })
        );
        this.addEntry(new ConfigEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.directional_sources"),
                directionalSources,
                config.directionalSources,
                TextUtils.multiLine("gui.plasmo_voice.advanced.directional_sources.tooltip", 5),
                (button, element) -> {
                    // kill all queues to prevent possible problems
                    SocketClientUDPQueue.closeAll();

                    directionalSourcesAngle.active = config.directionalSources.get();
                    ((ToggleButton) element).updateValue();
                })
        );
        this.addEntry(new ConfigEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.directional_sources_angle"),
                directionalSourcesAngle,
                config.directionalSourcesAngle,
                TextUtils.multiLine("gui.plasmo_voice.advanced.directional_sources_angle.tooltip", 4),
                (button, element) -> {
                    // kill all queues to prevent possible problems
                    SocketClientUDPQueue.closeAll();

                    ((ConfigIntegerSlider) element).updateValue();
                })
        );

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.advanced.visual")));
        this.addEntry(new ConfigEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.visual.distance"),
                new ToggleButton(0, 0, 97, 20, config.visualizeDistance,
                        toggled -> {}),
                config.visualizeDistance,
                (button, element) -> {
                    ((ToggleButton) element).updateValue();
                })
        );

        this.addEntry(new CategoryEntry(new TranslatableComponent("gui.plasmo_voice.advanced.ui")));
        this.addEntry(new ConfigEntry(
                new TranslatableComponent("gui.plasmo_voice.advanced.ui.priority"),
                new ToggleButton(0, 0, 97, 20, config.showPriorityVolume,
                        toggled -> {
                            parent.updateGeneralTab();
                        }),
                config.showPriorityVolume,
                TextUtils.multiLine("gui.plasmo_voice.advanced.ui.priority.tooltip", 2),
                (button, element) -> {
                    ((ToggleButton) element).updateValue();
                    parent.updateGeneralTab();
                })
        );
    }
}

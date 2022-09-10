package su.plo.voice.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.OutputDevice;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.GuiUtil;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widget.ToggleButton;

public final class AdvancedTabWidget extends TabWidget {

    private final PlasmoVoiceClient voiceClient;
    private final DeviceManager devices;
    private final ClientConfig config;

    public AdvancedTabWidget(Minecraft minecraft,
                             VoiceSettingsScreen parent,
                             PlasmoVoiceClient voiceClient,
                             ClientConfig config) {
        super(minecraft, parent);

        this.voiceClient = voiceClient;
        this.devices = voiceClient.getDeviceManager();
        this.config = config;
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(Component.translatable("gui.plasmovoice.advanced.compressor")));
        addEntry(createIntSliderWidget(
                "gui.plasmovoice.advanced.compressor_threshold",
                "gui.plasmovoice.advanced.compressor_threshold.tooltip",
                config.getAdvanced().getCompressorThreshold(),
                "dB"
        ));
        addEntry(createIntSliderWidget(
                "gui.plasmovoice.advanced.limiter_threshold",
                "gui.plasmovoice.advanced.limiter_threshold.tooltip",
                config.getAdvanced().getLimiterThreshold(),
                "dB"
        ));

        addEntry(new CategoryEntry(Component.translatable("gui.plasmovoice.advanced.audio_engine")));
        addEntry(createIntSliderWidget(
                "gui.plasmovoice.advanced.directional_sources_angle",
                "gui.plasmovoice.advanced.directional_sources_angle.tooltip",
                config.getAdvanced().getDirectionalSourcesAngle(),
                ""
        ));
        addEntry(createStereoToMonoSources());
    }

    private OptionEntry<ToggleButton> createStereoToMonoSources() {
        Runnable onUpdate = () -> {
            devices.<OutputDevice<?>>getDevices(DeviceType.OUTPUT)
                    .forEach(OutputDevice::closeSources);
        };

        ToggleButton toggleButton = new ToggleButton(
                0,
                0,
                97,
                20,
                config.getAdvanced().getStereoSourcesToMono(),
                (toggled) -> onUpdate.run()
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.advanced.stereo_sources_to_mono"),
                toggleButton,
                config.getAdvanced().getStereoSourcesToMono(),
                GuiUtil.multiLineTooltip("gui.plasmovoice.advanced.stereo_sources_to_mono.tooltip"),
                (button, element) -> onUpdate.run()
        );
    }
}

package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.OutputDevice;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.ToggleButton;

import java.util.List;

public final class AdvancedTabWidget extends TabWidget {

    private static final List<MinecraftTextComponent> ICONS_LIST = ImmutableList.of(
            MinecraftTextComponent.translatable("gui.plasmovoice.advanced.show_icons.hud"),
            MinecraftTextComponent.translatable("gui.plasmovoice.advanced.show_icons.always"),
            MinecraftTextComponent.translatable("gui.plasmovoice.advanced.show_icons.hidden")
    );

    private final DeviceManager devices;

    public AdvancedTabWidget(VoiceSettingsScreen parent,
                             PlasmoVoiceClient voiceClient,
                             VoiceClientConfig config) {
        super(parent, voiceClient, config);

        this.devices = voiceClient.getDeviceManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.advanced.visual")));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.visualize_voice_distance"),
                null,
                config.getAdvanced().getVisualizeVoiceDistance()
        ));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.visualize_voice_distance_on_join"),
                null,
                config.getAdvanced().getVisualizeVoiceDistanceOnJoin()
        ));

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.advanced.audio_engine")));
        addEntry(createIntSliderWidget(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.directional_sources_angle"),
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.directional_sources_angle.tooltip"),
                config.getAdvanced().getDirectionalSourcesAngle(),
                ""
        ));
        addEntry(createStereoToMonoSources());
        addEntry(createPanning());

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.advanced.exponential_volume")));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.exponential_volume.volume_slider"),
                null,
                config.getAdvanced().getExponentialVolumeSlider()
        ));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.exponential_volume.distance_gain"),
                null,
                config.getAdvanced().getExponentialDistanceGain()
        ));

//        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.advanced.compressor")));
//        addEntry(createIntSliderWidget(
//                "gui.plasmovoice.advanced.compressor_threshold",
//                "gui.plasmovoice.advanced.compressor_threshold.tooltip",
//                config.getAdvanced().getCompressorThreshold(),
//                "dB"
//        ));
//        addEntry(createIntSliderWidget(
//                "gui.plasmovoice.advanced.limiter_threshold",
//                "gui.plasmovoice.advanced.limiter_threshold.tooltip",
//                config.getAdvanced().getLimiterThreshold(),
//                "dB"
//        ));
    }

    private OptionEntry<ToggleButton> createStereoToMonoSources() {
        Runnable onUpdate = () -> {
            devices.<OutputDevice<?>>getDevices(DeviceType.OUTPUT)
                    .forEach(OutputDevice::closeSourcesAsync);
        };

        ToggleButton toggleButton = new ToggleButton(
                config.getAdvanced().getStereoSourcesToMono(),
                0,
                0,
                ELEMENT_WIDTH,
                20,
                (toggled) -> onUpdate.run()
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.stereo_sources_to_mono"),
                toggleButton,
                config.getAdvanced().getStereoSourcesToMono(),
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.stereo_sources_to_mono.tooltip"),
                (button, element) -> onUpdate.run()
        );
    }

    private OptionEntry<ToggleButton> createPanning() {
        Runnable onUpdate = () -> {
            devices.<OutputDevice<?>>getDevices(DeviceType.OUTPUT)
                    .forEach(OutputDevice::closeSourcesAsync);
        };

        ToggleButton toggleButton = new ToggleButton(
                config.getAdvanced().getPanning(),
                0,
                0,
                ELEMENT_WIDTH,
                20,
                (toggled) -> onUpdate.run()
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced.panning"),
                toggleButton,
                config.getAdvanced().getPanning(),
                (button, element) -> onUpdate.run()
        );
    }
}

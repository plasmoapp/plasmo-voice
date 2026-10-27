package su.plo.voice.client.gui.settings.tab;

import su.plo.lib.mod.client.gui.components.CompositeColumnWidget;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.device.OutputDevice;
import su.plo.voice.api.client.config.OverlappingSourceTypes;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;

public final class AdvancedTabWidget extends TabWidget {

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

        CompositeColumnWidget visual = addCategory(McTextComponent.translatable("gui.plasmovoice.advanced.visual"));
        visual.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.advanced.visualize_voice_distance"),
                null,
                config.getAdvanced().getVisualizeVoiceDistance()
        ));
        visual.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.advanced.visualize_voice_distance_on_join"),
                null,
                config.getAdvanced().getVisualizeVoiceDistanceOnJoin()
        ));

        CompositeColumnWidget audioEngine = addCategory(McTextComponent.translatable("gui.plasmovoice.advanced.audio_engine"));
        audioEngine.add(createIntSliderWidget(
                McTextComponent.translatable("gui.plasmovoice.advanced.directional_sources_angle"),
                McTextComponent.translatable("gui.plasmovoice.advanced.directional_sources_angle.tooltip"),
                config.getAdvanced().getDirectionalSourcesAngle(),
                ""
        ));
        audioEngine.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.advanced.stereo_sources_to_mono"),
                McTextComponent.translatable("gui.plasmovoice.advanced.stereo_sources_to_mono.tooltip"),
                config.getAdvanced().getStereoSourcesToMono(),
                toggled -> devices.getOutputDevice().ifPresent(OutputDevice::closeSourcesAsync)
        ));
        audioEngine.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.advanced.panning"),
                null,
                config.getAdvanced().getPanning()
        ));
        audioEngine.add(createDropDownEntry(
                McTextComponent.translatable("gui.plasmovoice.advanced.source_types_overlap"),
                McTextComponent.translatable("gui.plasmovoice.advanced.source_types_overlap.tooltip"),
                OverlappingSourceTypes.class,
                OverlappingSourceTypes.getTextElements(),
                config.getAdvanced().getSourceTypesOverlap(),
                false
        ));

        CompositeColumnWidget exponentialVolume = addCategory(
                McTextComponent.translatable("gui.plasmovoice.advanced.exponential_volume")
        );
        exponentialVolume.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.advanced.exponential_volume.volume_slider"),
                null,
                config.getAdvanced().getExponentialVolumeSlider()
        ));
        exponentialVolume.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.advanced.exponential_volume.distance_gain"),
                null,
                config.getAdvanced().getExponentialDistanceGain()
        ));

//        addEntry(new CategoryEntry(McTextComponent.translatable("gui.plasmovoice.advanced.compressor")));
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
}

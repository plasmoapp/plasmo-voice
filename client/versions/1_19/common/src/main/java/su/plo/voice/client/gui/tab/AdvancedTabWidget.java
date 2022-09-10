package su.plo.voice.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.VoiceSettingsScreen;

public final class AdvancedTabWidget extends TabWidget {

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    public AdvancedTabWidget(Minecraft minecraft,
                             VoiceSettingsScreen parent,
                             PlasmoVoiceClient voiceClient,
                             ClientConfig config) {
        super(minecraft, parent);

        this.voiceClient = voiceClient;
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
    }
}

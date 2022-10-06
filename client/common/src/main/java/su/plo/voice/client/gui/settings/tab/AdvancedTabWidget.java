package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiUtil;
import su.plo.lib.client.gui.components.Button;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.OutputDevice;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.settings.ActivationIconPositionScreen;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.DropDownWidget;
import su.plo.voice.client.gui.settings.widget.ToggleButton;

import java.util.List;

public final class AdvancedTabWidget extends TabWidget {

    private static final List<TextComponent> ICONS_LIST = ImmutableList.of(
            TextComponent.translatable("gui.plasmovoice.advanced.show_icons.hud"),
            TextComponent.translatable("gui.plasmovoice.advanced.show_icons.always"),
            TextComponent.translatable("gui.plasmovoice.advanced.show_icons.hidden")
    );

    private final DeviceManager devices;

    public AdvancedTabWidget(MinecraftClientLib minecraft,
                             VoiceSettingsScreen parent,
                             PlasmoVoiceClient voiceClient,
                             ClientConfig config) {
        super(minecraft, parent, voiceClient, config);

        this.devices = voiceClient.getDeviceManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(TextComponent.translatable("gui.plasmovoice.advanced.icons")));
        addEntry(createShowIcons());
        addEntry(createActivationIconPosition());

        addEntry(new CategoryEntry(TextComponent.translatable("gui.plasmovoice.advanced.visual")));
        addEntry(createToggleEntry(
                "gui.plasmovoice.advanced.visualize_voice_distance",
                null,
                config.getAdvanced().getVisualizeVoiceDistance()
        ));

        addEntry(new CategoryEntry(TextComponent.translatable("gui.plasmovoice.advanced.audio_engine")));
        addEntry(createIntSliderWidget(
                "gui.plasmovoice.advanced.directional_sources_angle",
                "gui.plasmovoice.advanced.directional_sources_angle.tooltip",
                config.getAdvanced().getDirectionalSourcesAngle(),
                ""
        ));
        addEntry(createStereoToMonoSources());

        addEntry(new CategoryEntry(TextComponent.translatable("gui.plasmovoice.advanced.compressor")));
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
    }

    private OptionEntry<DropDownWidget> createShowIcons() {
        DropDownWidget dropdown = new DropDownWidget(
                minecraft,
                parent,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                ICONS_LIST.get(config.getAdvanced().getShowIcons().value()),
                ICONS_LIST,
                true,
                (index) -> config.getAdvanced().getShowIcons().set(index)
        );

        return new OptionEntry<>(
                TextComponent.translatable("gui.plasmovoice.advanced.show_icons"),
                dropdown,
                config.getAdvanced().getShowIcons(),
                (button, element) -> element.setText(ICONS_LIST.get(config.getAdvanced().getShowIcons().value()))
        );
    }

    private OptionEntry<Button> createActivationIconPosition() {
        Button button = new Button(
                minecraft,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                TextComponent.translatable(config.getAdvanced().getActivationIconPosition().value().getTranslation()),
                (btn) -> {
                    minecraft.setScreen(new ActivationIconPositionScreen(minecraft, parent, config.getAdvanced().getActivationIconPosition()));
                },
                Button.NO_TOOLTIP
        );

        return new OptionEntry<>(
                TextComponent.translatable("gui.plasmovoice.advanced.icon_position"),
                button,
                config.getAdvanced().getActivationIconPosition(),
                (btn, element) -> element.setText(
                        TextComponent.translatable(config.getAdvanced().getActivationIconPosition().value().getTranslation())
                )
        );
    }

    private OptionEntry<ToggleButton> createStereoToMonoSources() {
        Runnable onUpdate = () -> {
            devices.<OutputDevice<?>>getDevices(DeviceType.OUTPUT)
                    .forEach(OutputDevice::closeSources);
        };

        ToggleButton toggleButton = new ToggleButton(
                minecraft,
                config.getAdvanced().getStereoSourcesToMono(),
                0,
                0,
                ELEMENT_WIDTH,
                20,
                (toggled) -> onUpdate.run()
        );

        return new OptionEntry<>(
                TextComponent.translatable("gui.plasmovoice.advanced.stereo_sources_to_mono"),
                toggleButton,
                config.getAdvanced().getStereoSourcesToMono(),
                GuiUtil.multiLineTooltip(minecraft.getLanguage(), "gui.plasmovoice.advanced.stereo_sources_to_mono.tooltip"),
                (button, element) -> onUpdate.run()
        );
    }
}
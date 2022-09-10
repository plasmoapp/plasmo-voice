package su.plo.voice.client.gui.tab;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.OutputDevice;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.ActivationIconPositionScreen;
import su.plo.voice.client.gui.GuiUtil;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widget.DropDownWidget;
import su.plo.voice.client.gui.widget.ToggleButton;

import java.util.List;

public final class AdvancedTabWidget extends TabWidget {

    private static final List<Component> ICONS_LIST = ImmutableList.of(
            Component.translatable("gui.plasmovoice.advanced.show_icons.hud"),
            Component.translatable("gui.plasmovoice.advanced.show_icons.always"),
            Component.translatable("gui.plasmovoice.advanced.show_icons.hidden")
    );

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

        addEntry(new CategoryEntry(Component.translatable("gui.plasmovoice.advanced.icons")));
        addEntry(createShowIcons());
        addEntry(createActivationIconPosition());

        addEntry(new CategoryEntry(Component.translatable("gui.plasmovoice.advanced.audio_engine")));
        addEntry(createIntSliderWidget(
                "gui.plasmovoice.advanced.directional_sources_angle",
                "gui.plasmovoice.advanced.directional_sources_angle.tooltip",
                config.getAdvanced().getDirectionalSourcesAngle(),
                ""
        ));
        addEntry(createStereoToMonoSources());

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
    }

    private OptionEntry<DropDownWidget> createShowIcons() {
        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                97,
                20,
                ICONS_LIST.get(config.getAdvanced().getShowIcons().value()),
                ICONS_LIST,
                true,
                (index) -> config.getAdvanced().getShowIcons().set(index)
        );

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.advanced.show_icons"),
                dropdown,
                config.getAdvanced().getShowIcons(),
                (button, element) -> element.setMessage(ICONS_LIST.get(config.getAdvanced().getShowIcons().value()))
        );
    }

    private OptionEntry<Button> createActivationIconPosition() {
        Button button = new Button(
                0,
                0,
                97,
                20,
                Component.translatable(config.getAdvanced().getActivationIconPosition().value().getTranslation()),
                (btn) -> {
                    minecraft.setScreen(new ActivationIconPositionScreen(parent, config.getAdvanced().getActivationIconPosition()));
                });

        return new OptionEntry<>(
                Component.translatable("gui.plasmovoice.advanced.icon_position"),
                button,
                config.getAdvanced().getActivationIconPosition(),
                (btn, element) -> element.setMessage(
                        Component.translatable(config.getAdvanced().getActivationIconPosition().value().getTranslation())
                )
        );
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

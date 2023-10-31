package su.plo.voice.client.gui.settings.tab;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.config.addon.VoiceAddonConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.DropDownWidget;

import java.util.stream.Collectors;

public final class AddonsTabWidget extends TabWidget {

    private final BaseVoiceClient voiceClient;

    public AddonsTabWidget(
            @NotNull VoiceSettingsScreen parent,
            @NotNull BaseVoiceClient voiceClient,
            @NotNull VoiceClientConfig config
    ) {
        super(parent, voiceClient, config);

        this.voiceClient = voiceClient;
    }

    @Override
    public void init() {
        super.init();

        voiceClient.getAddonConfigs()
                .forEach((addonId, addonConfig) -> createAddonEntry((VoiceAddonConfig) addonConfig));
    }

    private void createAddonEntry(@NotNull VoiceAddonConfig addonConfig) {
        addEntry(new CategoryEntry(McTextComponent.translatable(addonConfig.getAddon().getName())));

        addonConfig.getWidgets().forEach((widget) -> {
            switch (widget.getType()) {
                case INT_SLIDER:
                    addEntry(createIntSliderWidget(
                            widget.getLabel(),
                            widget.getTooltip(),
                            (IntConfigEntry) widget.getConfigEntry(),
                            ((VoiceAddonConfig.ConfigSliderWidget) widget).getSuffix()
                    ));
                    break;
                case VOLUME_SLIDER:
                    addEntry(createVolumeSlider(
                            widget.getLabel(),
                            widget.getTooltip(),
                            (DoubleConfigEntry) widget.getConfigEntry(),
                            ((VoiceAddonConfig.ConfigSliderWidget) widget).getSuffix()
                    ));
                    break;
                case TOGGLE:
                    addEntry(createToggleEntry(
                            widget.getLabel(),
                            widget.getTooltip(),
                            (BooleanConfigEntry) widget.getConfigEntry()
                    ));
                    break;
                case DROPDOWN:
                    addEntry(createDropDownEntry((VoiceAddonConfig.ConfigDropDownWidget) widget));
                    break;
            }
        });
    }

    private OptionEntry<DropDownWidget> createDropDownEntry(VoiceAddonConfig.ConfigDropDownWidget widget) {
        IntConfigEntry configEntry = (IntConfigEntry) widget.getConfigEntry();

        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                McTextComponent.translatable(widget.getElements().get(configEntry.value())),
                widget.getElements()
                        .stream()
                        .map(McTextComponent::translatable)
                        .collect(Collectors.toList()),
                widget.isElementTooltip(),
                configEntry::set
        );

        return new OptionEntry<>(
                widget.getLabel(),
                dropdown,
                configEntry,
                (button, element) -> element.setText(McTextComponent.translatable(widget.getElements().get(configEntry.value())))
        );
    }
}

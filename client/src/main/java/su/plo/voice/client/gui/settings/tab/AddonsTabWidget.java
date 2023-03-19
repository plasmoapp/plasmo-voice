package su.plo.voice.client.gui.settings.tab;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.addon.VoiceAddonConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.DropDownWidget;

import java.util.stream.Collectors;

public final class AddonsTabWidget extends TabWidget {

    private final BaseVoiceClient voiceClient;

    public AddonsTabWidget(@NotNull VoiceSettingsScreen parent,
                           @NotNull BaseVoiceClient voiceClient,
                           @NotNull ClientConfig config) {
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
        addEntry(new CategoryEntry(MinecraftTextComponent.translatable(addonConfig.getAddon().getName())));

        addonConfig.getWidgets().forEach((widget) -> {
            switch (widget.getType()) {
                case INT_SLIDER:
                    addEntry(createIntSliderWidget(
                            MinecraftTextComponent.translatable(widget.getTranslatable()),
                            MinecraftTextComponent.translatable(widget.getTooltipTranslatable()),
                            (IntConfigEntry) widget.getConfigEntry(),
                            ((VoiceAddonConfig.ConfigSliderWidget) widget).getSuffix()
                    ));
                    break;
                case VOLUME_SLIDER:
                    addEntry(createVolumeSlider(
                            MinecraftTextComponent.translatable(widget.getTranslatable()),
                            MinecraftTextComponent.translatable(widget.getTooltipTranslatable()),
                            (DoubleConfigEntry) widget.getConfigEntry(),
                            ((VoiceAddonConfig.ConfigSliderWidget) widget).getSuffix()
                    ));
                    break;
                case TOGGLE:
                    addEntry(createToggleEntry(
                            MinecraftTextComponent.translatable(widget.getTranslatable()),
                            MinecraftTextComponent.translatable(widget.getTooltipTranslatable()),
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
                MinecraftTextComponent.translatable(widget.getElements().get(configEntry.value())),
                widget.getElements()
                        .stream()
                        .map(MinecraftTextComponent::translatable)
                        .collect(Collectors.toList()),
                widget.isElementTooltip(),
                configEntry::set
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable(widget.getTranslatable()),
                dropdown,
                configEntry,
                (button, element) -> element.setText(MinecraftTextComponent.translatable(widget.getElements().get(configEntry.value())))
        );
    }
}

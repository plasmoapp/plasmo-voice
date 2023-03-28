package su.plo.voice.client.config.addon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.client.config.addon.AddonConfig;
import su.plo.voice.client.config.VoiceClientConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class VoiceAddonConfig implements AddonConfig {

    @Getter
    private final AddonContainer addon;
    private final VoiceClientConfig.Addons.Addon config;

    private final Map<String, ConfigWidget> widgetsByTranslatable = Maps.newConcurrentMap();
    @Getter
    private final List<ConfigWidget> widgets = Lists.newCopyOnWriteArrayList();

    @Override
    public @NotNull IntConfigEntry addIntSlider(@NotNull String translatable,
                                                @Nullable String tooltipTranslatable,
                                                @NotNull String suffix,
                                                int defaultValue, int min, int max) {
        checkWidgetExists(translatable);

        IntConfigEntry configEntry = config.getEntry(translatable)
                .map(IntConfigEntry.class::cast)
                .orElseGet(createEntry(translatable, () -> new IntConfigEntry(defaultValue, min, max)));
        configEntry.setDefault(defaultValue, min, max);

        addWidget(
                new ConfigSliderWidget(
                        ConfigWidget.Type.INT_SLIDER,
                        translatable,
                        tooltipTranslatable,
                        configEntry,
                        suffix
                )
        );

        return configEntry;
    }

    @Override
    public @NotNull DoubleConfigEntry addVolumeSlider(@NotNull String translatable,
                                                      @Nullable String tooltipTranslatable,
                                                      @NotNull String suffix,
                                                      double defaultValue, double min, double max) {
        checkWidgetExists(translatable);

        DoubleConfigEntry configEntry = config.getEntry(translatable)
                .map(DoubleConfigEntry.class::cast)
                .orElseGet(createEntry(translatable, () -> new DoubleConfigEntry(defaultValue, min, max)));
        configEntry.setDefault(defaultValue, min, max);

        addWidget(
                new ConfigSliderWidget(
                        ConfigWidget.Type.VOLUME_SLIDER,
                        translatable,
                        tooltipTranslatable,
                        configEntry,
                        suffix
                )
        );

        return configEntry;
    }

    @Override
    public @NotNull BooleanConfigEntry addToggle(@NotNull String translatable,
                                                 @Nullable String tooltipTranslatable,
                                                 boolean defaultValue) {
        checkWidgetExists(translatable);

        BooleanConfigEntry configEntry = config.getEntry(translatable)
                .map(BooleanConfigEntry.class::cast)
                .orElseGet(createEntry(translatable, () -> new BooleanConfigEntry(defaultValue)));
        configEntry.setDefault(defaultValue);

        addWidget(
                new ConfigWidget(
                        ConfigWidget.Type.TOGGLE,
                        translatable,
                        tooltipTranslatable,
                        configEntry
                )
        );

        return configEntry;
    }

    @Override
    public @NotNull IntConfigEntry addDropDown(@NotNull String translatable,
                                      @Nullable String tooltipTranslatable,
                                      @NotNull List<String> elements,
                                      boolean elementTooltip,
                                      int defaultValue) {
        checkWidgetExists(translatable);

        IntConfigEntry configEntry = config.getEntry(translatable)
                .map(IntConfigEntry.class::cast)
                .orElseGet(createEntry(translatable, () -> new IntConfigEntry(defaultValue, 0, 0)));
        configEntry.setDefault(defaultValue);

        addWidget(
                new ConfigDropDownWidget(
                        translatable,
                        tooltipTranslatable,
                        configEntry,
                        elements,
                        elementTooltip
                )
        );

        return configEntry;
    }

    @Override
    public boolean removeWidget(@NotNull String translatable) {
        return Optional.ofNullable(widgetsByTranslatable.remove(translatable))
                .filter(widgets::remove)
                .isPresent();
    }

    @Override
    public <T extends ConfigEntry<?>> Optional<T> getValue(@NotNull String translatable) {
        ConfigWidget widget = widgetsByTranslatable.get(translatable);
        if (widget == null) return Optional.empty();

        return Optional.of((T) widget.getConfigEntry());
    }

    @Override
    public void clear() {
        widgets.clear();
    }

    private void addWidget(@NotNull ConfigWidget widget) {
        widgets.add(widget);
        widgetsByTranslatable.put(widget.getTranslatable(), widget);
    }

    private <T extends ConfigEntry<?>> Supplier<T> createEntry(@NotNull String translatable, @NotNull Supplier<T> supplier) {
        return () -> {
            T entry = supplier.get();
            config.setEntry(translatable, entry);
            return entry;
        };
    }

    private void checkWidgetExists(@NotNull String translatable) {
        if (widgetsByTranslatable.containsKey(translatable))
            throw new IllegalArgumentException("Widget " + translatable + " already exist");
    }

    @RequiredArgsConstructor
    public static class ConfigWidget {

        @Getter
        private final @NotNull Type type;
        @Getter
        private final @NotNull String translatable;
        @Getter
        private final @Nullable String tooltipTranslatable;
        @Getter
        private final @NotNull ConfigEntry<?> configEntry;

        public enum Type {
            INT_SLIDER,
            VOLUME_SLIDER,
            TOGGLE,
            DROPDOWN
        }
    }

    public static class ConfigSliderWidget extends ConfigWidget {

        @Getter
        private final @NotNull String suffix;

        public ConfigSliderWidget(@NotNull Type type,
                                  @NotNull String translatable,
                                  @Nullable String tooltipTranslatable,
                                  @NotNull ConfigEntry<?> configEntry,
                                  @NotNull String suffix) {
            super(type, translatable, tooltipTranslatable, configEntry);

            this.suffix = suffix;
        }
    }

    public static class ConfigDropDownWidget extends ConfigWidget {

        @Getter
        private final @NotNull List<String> elements;
        @Getter
        private final boolean elementTooltip;

        public ConfigDropDownWidget(@NotNull String translatable,
                                    @Nullable String tooltipTranslatable,
                                    @NotNull ConfigEntry<?> configEntry,
                                    @NotNull List<String> elements,
                                    boolean elementTooltip) {
            super(Type.DROPDOWN, translatable, tooltipTranslatable, configEntry);

            this.elements = elements;
            this.elementTooltip = elementTooltip;
        }
    }
}

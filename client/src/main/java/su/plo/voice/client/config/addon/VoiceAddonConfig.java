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
import su.plo.slib.api.chat.component.McTextComponent;
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

    private final Map<String, ConfigWidget> widgetsById = Maps.newConcurrentMap();
    @Getter
    private final List<ConfigWidget> widgets = Lists.newCopyOnWriteArrayList();

    @Override
    public @NotNull IntConfigEntry addIntSlider(
            @NotNull String widgetId,
            @NotNull McTextComponent label,
            @Nullable McTextComponent tooltip,
            @NotNull String suffix,
            int defaultValue,
            int min,
            int max
    ) {
        checkWidgetExists(widgetId);

        IntConfigEntry configEntry = config.getEntry(widgetId)
                .map(IntConfigEntry.class::cast)
                .orElseGet(createEntry(widgetId, () -> new IntConfigEntry(defaultValue, min, max)));
        configEntry.setDefault(defaultValue, min, max);

        addWidget(
                widgetId,
                new ConfigSliderWidget(
                        ConfigWidget.Type.INT_SLIDER,
                        label,
                        tooltip,
                        configEntry,
                        suffix
                )
        );

        return configEntry;
    }

    @Override
    public @NotNull DoubleConfigEntry addVolumeSlider(
            @NotNull String widgetId,
            @NotNull McTextComponent label,
            @Nullable McTextComponent tooltip,
            @NotNull String suffix,
            double defaultValue,
            double min,
            double max
    ) {
        checkWidgetExists(widgetId);

        DoubleConfigEntry configEntry = config.getEntry(widgetId)
                .map(DoubleConfigEntry.class::cast)
                .orElseGet(createEntry(widgetId, () -> new DoubleConfigEntry(defaultValue, min, max)));
        configEntry.setDefault(defaultValue, min, max);

        addWidget(
                widgetId,
                new ConfigSliderWidget(
                        ConfigWidget.Type.VOLUME_SLIDER,
                        label,
                        tooltip,
                        configEntry,
                        suffix
                )
        );

        return configEntry;
    }

    @Override
    public @NotNull BooleanConfigEntry addToggle(
            @NotNull String widgetId,
            @NotNull McTextComponent label,
            @Nullable McTextComponent tooltip,
            boolean defaultValue
    ) {
        checkWidgetExists(widgetId);

        BooleanConfigEntry configEntry = config.getEntry(widgetId)
                .map(BooleanConfigEntry.class::cast)
                .orElseGet(createEntry(widgetId, () -> new BooleanConfigEntry(defaultValue)));
        configEntry.setDefault(defaultValue);

        addWidget(
                widgetId,
                new ConfigWidget(
                        ConfigWidget.Type.TOGGLE,
                        label,
                        tooltip,
                        configEntry
                )
        );

        return configEntry;
    }

    @Override
    public @NotNull IntConfigEntry addDropDown(
            @NotNull String widgetId,
            @NotNull McTextComponent label,
            @Nullable McTextComponent tooltip,
            @NotNull List<String> elements,
            boolean elementTooltip,
            int defaultValueIndex
    ) {
        checkWidgetExists(widgetId);

        IntConfigEntry configEntry = config.getEntry(widgetId)
                .map(IntConfigEntry.class::cast)
                .orElseGet(createEntry(widgetId, () -> new IntConfigEntry(defaultValueIndex, 0, 0)));
        configEntry.setDefault(defaultValueIndex);

        addWidget(
                widgetId,
                new ConfigDropDownWidget(
                        label,
                        tooltip,
                        configEntry,
                        elements,
                        elementTooltip
                )
        );

        return configEntry;
    }

    @Override
    public boolean removeWidget(@NotNull String translatable) {
        return Optional.ofNullable(widgetsById.remove(translatable))
                .filter(widgets::remove)
                .isPresent();
    }

    @Override
    public <T extends ConfigEntry<?>> Optional<T> getWidgetConfigEntry(@NotNull String translatable) {
        ConfigWidget widget = widgetsById.get(translatable);
        if (widget == null) return Optional.empty();

        return Optional.of((T) widget.getConfigEntry());
    }

    @Override
    public void clear() {
        widgets.clear();
    }

    private void addWidget(@NotNull String id, @NotNull ConfigWidget widget) {
        widgets.add(widget);
        widgetsById.put(id, widget);
    }

    private <T extends ConfigEntry<?>> Supplier<T> createEntry(@NotNull String widgetId, @NotNull Supplier<T> supplier) {
        return () -> {
            T entry = supplier.get();
            config.setEntry(widgetId, entry);
            return entry;
        };
    }

    private void checkWidgetExists(@NotNull String translatable) {
        if (widgetsById.containsKey(translatable))
            throw new IllegalArgumentException("Widget " + translatable + " already exist");
    }

    @RequiredArgsConstructor
    public static class ConfigWidget {

        @Getter
        private final @NotNull Type type;
        @Getter
        private final @NotNull McTextComponent label;
        @Getter
        private final @Nullable McTextComponent tooltip;
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

        public ConfigSliderWidget(
                @NotNull Type type,
                @NotNull McTextComponent label,
                @Nullable McTextComponent tooltip,
                @NotNull ConfigEntry<?> configEntry,
                @NotNull String suffix
        ) {
            super(type, label, tooltip, configEntry);

            this.suffix = suffix;
        }
    }

    public static class ConfigDropDownWidget extends ConfigWidget {

        @Getter
        private final @NotNull List<String> elements;
        @Getter
        private final boolean elementTooltip;

        public ConfigDropDownWidget(
                @NotNull McTextComponent label,
                @Nullable McTextComponent tooltip,
                @NotNull ConfigEntry<?> configEntry,
                @NotNull List<String> elements,
                boolean elementTooltip
        ) {
            super(Type.DROPDOWN, label, tooltip, configEntry);

            this.elements = elements;
            this.elementTooltip = elementTooltip;
        }
    }
}

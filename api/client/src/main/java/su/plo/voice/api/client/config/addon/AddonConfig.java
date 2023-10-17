package su.plo.voice.api.client.config.addon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.config.entry.IntConfigEntry;

import java.util.List;
import java.util.Optional;


// todo: docs & textfields
/**
 * Adds a custom config widget under the addon's block to the Plasmo Voice menu in addons tab.
 */
public interface AddonConfig {

    @NotNull IntConfigEntry addIntSlider(@NotNull String translatable,
                                         @Nullable String tooltipTranslatable,
                                         @NotNull String suffix,
                                         int defaultValue, int min, int max);

    @NotNull DoubleConfigEntry addVolumeSlider(@NotNull String translatable,
                                               @Nullable String tooltipTranslatable,
                                               @NotNull String suffix,
                                               double defaultValue, double min, double max);

    @NotNull BooleanConfigEntry addToggle(@NotNull String translatable,
                                          @Nullable String tooltipTranslatable,
                                          boolean defaultValue);

    @NotNull IntConfigEntry addDropDown(@NotNull String translatable,
                                        @Nullable String tooltipTranslatable,
                                        @NotNull List<String> elements,
                                        boolean elementTooltip,
                                        int defaultValue);

//    void addTextField(@NotNull String translatable,
//                      @Nullable String tooltipTranslatable,
//                      @NotNull String defaultValue);
//
//    void addNumberTextField(@NotNull String translatable,
//                            @Nullable String tooltipTranslatable,
//                            int defaultValue, int min, int max);

    boolean removeWidget(@NotNull String translatable);

    <T extends ConfigEntry<?>> Optional<T> getValue(@NotNull String translatable);

    void clear();
}

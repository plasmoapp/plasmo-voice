package su.plo.voice.api.client.config.addon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.slib.api.chat.component.McTextComponent;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Adds a custom config widget under the addon's block to the Plasmo Voice menu in addons tab.
 */
public interface AddonConfig {

    /**
     * The regular expression pattern that widget ids must match.
     * A widget id must start with a lowercase letter and may
     * contain only lowercase letters, digits, hyphens, and underscores.
     * It should be between 4 and 32 characters long.
     */
    @NotNull Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{4,31}");


    /**
     * Adds an integer slider to the addon's configuration.
     *
     * @param widgetId     Unique identifier for the widget.
     * @param label        The label displayed for the slider.
     * @param tooltip      The tooltip displayed for the slider.
     * @param suffix       The suffix displayed after the value on the slider.
     * @param defaultValue The default value of the slider.
     * @param min          The minimum value for the slider.
     * @param max          The maximum value for the slider.
     * @return A configuration entry associated with the created widget.
     */
    @NotNull IntConfigEntry addIntSlider(
            @NotNull String widgetId,
            @NotNull McTextComponent label,
            @Nullable McTextComponent tooltip,
            @NotNull String suffix,
            int defaultValue,
            int min,
            int max
    );


    /**
     * Adds a volume slider to the addon's configuration.
     *
     * @param widgetId     Unique identifier for the widget.
     * @param label        The label displayed for the slider.
     * @param tooltip      The tooltip displayed for the slider.
     * @param suffix       The suffix displayed after the value on the slider.
     * @param defaultValue The default value of the slider.
     * @param min          The minimum value for the slider.
     * @param max          The maximum value for the slider.
     * @return A configuration entry associated with the created widget.
     */
    @NotNull DoubleConfigEntry addVolumeSlider(
            @NotNull String widgetId,
            @NotNull McTextComponent label,
            @Nullable McTextComponent tooltip,
            @NotNull String suffix,
            double defaultValue,
            double min,
            double max
    );

    /**
     * Adds a toggle (boolean) widget to the addon's configuration.
     *
     * @param widgetId     Unique identifier for the widget.
     * @param label        The label displayed for the toggle.
     * @param tooltip      The tooltip displayed for the toggle.
     * @param defaultValue The default value of the toggle.
     * @return A configuration entry associated with the created widget.
     */
    @NotNull BooleanConfigEntry addToggle(
            @NotNull String widgetId,
            @NotNull McTextComponent label,
            @Nullable McTextComponent tooltip,
            boolean defaultValue
    );

    /**
     * Adds a drop-down list widget to the addon's configuration.
     *
     * @param widgetId          Unique identifier for the widget.
     * @param label             The label displayed for the drop-down list.
     * @param tooltip           The tooltip displayed for the drop-down list.
     * @param elements          The list of elements displayed in the drop-down list.
     * @param elementTooltip    If true, elements will display tooltips.
     * @param defaultValueIndex The default index selected for the drop-down list.
     * @return A configuration entry associated with the created widget.
     */
    @NotNull IntConfigEntry addDropDown(
            @NotNull String widgetId,
            @NotNull McTextComponent label,
            @Nullable McTextComponent tooltip,
            @NotNull List<String> elements,
            boolean elementTooltip,
            int defaultValueIndex
    );

    /**
     * Removes the specified widget from the configuration.
     *
     * @param widgetId Unique identifier for the widget to be removed.
     * @return {@code true} if the widget was successfully removed, {@code false} otherwise.
     */
    boolean removeWidget(@NotNull String widgetId);

    /**
     * Gets the configuration entry associated with the specified widget.
     *
     * @param widgetId Unique identifier of the widget.
     * @return An optional containing the configuration entry, or an empty optional if not found.
     */
    <T extends ConfigEntry<?>> Optional<T> getWidgetConfigEntry(@NotNull String widgetId);

    /**
     * Removes all widgets.
     */
    void clear();
}

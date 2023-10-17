package su.plo.voice.api.client.config.hotkey;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Clients hotkeys. They are available and can be changed in the Plasmo Voice menu in the hotkeys widgets.
 */
public interface Hotkeys {

    /**
     * Gets the currently pressed keys.
     *
     * @return A collection of currently pressed keys.
     */
    Collection<Hotkey.Key> getPressedKeys();

    /**
     * Retrieves a hotkey by its name.
     *
     * @param name The name of the hotkey to retrieve.
     * @return An optional containing the hotkey if found, or empty if not.
     */
    Optional<Hotkey> getHotkey(@NotNull String name);

    /**
     * Registers a new hotkey.
     *
     * @param name        The name of the hotkey.
     * @param keys        The list of keys associated with the hotkey.
     * @param category    The category to which the hotkey belongs.
     * @param anyContext  A flag indicating if the hotkey should trigger in any context (for example in any menu).
     * @return The registered hotkey.
     */
    @NotNull Hotkey register(@NotNull String name, List<Hotkey.Key> keys, @NotNull String category, boolean anyContext);

    /**
     * Resets the pressed state of all hotkeys.
     */
    void resetPressedStates();

    /**
     * Gets a map of hotkey categories, where each category is associated with a collection of hotkeys.
     *
     * @return A map of hotkey categories and their associated hotkeys.
     */
    @NotNull Map<String, Collection<Hotkey>> getCategories();
}

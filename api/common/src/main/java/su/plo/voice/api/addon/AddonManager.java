package su.plo.voice.api.addon;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.annotation.Addon;

import java.util.Optional;

/**
 * Manages Plasmo Voice addons
 */
public interface AddonManager {

    /**
     * Loads an addon
     *
     * @param addonObject object annotated with {@link Addon}
     */
    void load(@NotNull Object addonObject);

    /**
     * Checks if an addon is loaded
     * @return true if an addon is loaded
     */
    boolean isLoaded(@NotNull String id);

    /**
     * Gets an {@link AddonContainer} by its id
     *
     * @return the addon, if available
     */
    Optional<AddonContainer> getAddon(String id);

    /**
     * Gets an {@link AddonContainer} by its instance
     */
    Optional<AddonContainer> getAddon(Object instance);
}

package su.plo.voice.api.addon;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.annotation.Addon;

import java.util.Optional;

/**
 * Manages Plasmo Voice addons.
 */
public interface AddonManager {

    /**
     * Loads an addon specified by its annotated object.
     *
     * @param addonObject An object annotated with {@link Addon} representing the addon to be loaded.
     */
    void load(@NotNull Object addonObject);

    /**
     * Unloads a previously loaded addon specified by its annotated object.
     *
     * @param addonObject An object annotated with {@link Addon} representing the addon to be unloaded.
     */
    void unload(@NotNull Object addonObject);

    /**
     * Checks if an addon with the specified ID is currently loaded.
     *
     * @param id The unique ID of the addon to check.
     * @return {@code true} if the addon is currently loaded, {@code false} otherwise.
     */
    boolean isLoaded(@NotNull String id);

    /**
     * Retrieves an {@link AddonContainer} associated with the addon ID.
     *
     * @param id The unique ID of the addon to retrieve.
     * @return An optional containing the addon container if available, or an empty optional if the addon is not loaded.
     */
    Optional<AddonContainer> getAddon(@NotNull String id);

    /**
     * Retrieves an {@link AddonContainer} associated with the instance of an addon.
     *
     * @param instance The instance of the addon to retrieve.
     * @return An optional containing the addon container if available, or an empty optional if the addon is not loaded.
     */
    Optional<AddonContainer> getAddon(@NotNull Object instance);
}

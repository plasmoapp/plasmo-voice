package su.plo.voice.api.addon;

import su.plo.voice.api.addon.annotation.Addon;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a wrapper around an addon.
 */
public interface AddonContainer {

    /**
     * The regular expression pattern that addon IDs must match. An addon ID must start with a lowercase letter and may
     * contain only lowercase letters, digits, hyphens, and underscores. It should be between 4 and 32 characters long.
     */
    Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{2,31}");

    /**
     * Gets the unique ID of the addon. The addon ID should match the pattern defined by {@link #ID_PATTERN}.
     *
     * @return The addon ID.
     * @see Addon#id()
     */
    String getId();

    /**
     * Gets the name of the addon. The name may be a translatable key.
     *
     * @return The addon name.
     * @see Addon#name()
     */
    String getName();

    /**
     * Gets the scope of the addon, which indicates how it should be loaded.
     *
     * @return The addon scope.
     */
    AddonLoaderScope getScope();

    /**
     * Gets the version of the addon.
     *
     * @return The addon version.
     */
    String getVersion();

    /**
     * Gets the authors of the addon.
     *
     * @return A collection of addon authors.
     */
    Collection<String> getAuthors();

    /**
     * Gets a collection of all addon dependencies declared by the addon. These dependencies are specified using the
     * {@link AddonDependency} annotation.
     *
     * @return A collection of addon dependencies.
     * @see Addon#dependencies()
     */
    Collection<AddonDependency> getDependencies();


    /**
     * Returns the main class of the addon, which is the class that serves as the entry point for the addon's
     * functionality.
     *
     * @return The main class of the addon.
     */
    Class<?> getMainClass();

    /**
     * Returns an optional instance of the addon if it has been instantiated.
     *
     * @return An optional containing the addon instance, or an empty optional if not instantiated.
     */
    Optional<?> getInstance();
}

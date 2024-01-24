package su.plo.voice.api.addon;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.annotation.Addon;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a wrapper around an addon.
 */
public interface AddonContainer {

    /**
     * The regular expression pattern that addon ids must match.
     * An addon id must start with a lowercase letter and may
     * contain only lowercase letters, digits, hyphens, and underscores.
     * It should be between 4 and 32 characters long.
     */
    @NotNull Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{3,31}");

    /**
     * Gets the unique id of the addon. The addon id should match the pattern defined by {@link #ID_PATTERN}.
     *
     * @return The addon id.
     * @see Addon#id()
     */
    @NotNull String getId();

    /**
     * Gets the name of the addon. The name may be a translatable key.
     *
     * @return The addon name.
     * @see Addon#name()
     */
    @NotNull String getName();

    /**
     * Gets the scope of the addon, which indicates where it should be loaded.
     *
     * @return The addon scope.
     */
    @NotNull AddonLoaderScope getScope();

    /**
     * Gets the version of the addon.
     *
     * @return The addon version.
     */
    @NotNull String getVersion();

    /**
     * Gets the authors of the addon.
     *
     * @return A collection of addon authors.
     */
    @NotNull Collection<String> getAuthors();

    /**
     * Gets a collection of addon dependencies declared by the addon.
     *
     * @return A collection of addon dependencies.
     * @see Addon#dependencies()
     */
    @NotNull Collection<AddonDependency> getDependencies();

    /**
     * Returns the main class of the addon, which is the class that serves as the entry point for the addon's
     * functionality.
     *
     * @return The main class of the addon.
     */
    @NotNull Class<?> getMainClass();

    /**
     * Returns an optional instance of the addon if it has been instantiated.
     *
     * @return An optional containing the addon instance, or an empty optional if not instantiated.
     */
    Optional<?> getInstance();
}

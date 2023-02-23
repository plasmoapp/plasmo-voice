package su.plo.voice.api.addon;

import su.plo.voice.api.addon.annotation.Addon;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A wrapper around an addon
 */
public interface AddonContainer {

    /**
     * The pattern addon IDs must match
     */
    Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,16}");

    /**
     * Gets the addon id
     *
     * @return the addon id
     * @see Addon#id()
     */
    String getId();

    /**
     * Gets the addon name
     *
     * @return the addon name
     * @see Addon#name()
     */
    String getName();

    /**
     * Gets the addon scope
     *
     * @return the addon scope
     */
    AddonScope getScope();

    /**
     * Gets the addon version
     *
     * @return the addon version
     */
    String getVersion();

    /**
     * Gets the addon authors
     *
     * @return the addon authors
     */
    Collection<String> getAuthors();

    /**
     * Gets a {@link Collection} of all {@link AddonDependency} of the {@link Addon}
     *
     * @return the addon dependencies
     * @see Addon#dependencies()
     */
    Collection<AddonDependency> getDependencies();

    /**
     * Returns the addon's main class
     *
     * @return the addon's main class
     */
    Class<?> getMainClass();

    /**
     * Returns the addon's path
     *
     * @return the addon's path
     */
    Path getPath();

    /**
     * Returns the addon instance
     *
     * @return the addon instance
     */
    Optional<?> getInstance();
}

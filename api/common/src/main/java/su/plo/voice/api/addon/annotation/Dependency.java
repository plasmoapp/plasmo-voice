package su.plo.voice.api.addon.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify dependencies required by an addon.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Dependency {

    /**
     * Gets the id of the dependency.
     *
     * @return The dependency id.
     */
    @NotNull String id();

    /**
     * Checks whether this dependency is optional. Optional dependencies are not strictly required for the addon to
     * function, and the addon can work without them if they are not available.
     *
     * @return {@code true} if the dependency is optional; {@code false} otherwise.
     */
    boolean optional() default false;
}

package su.plo.voice.api.addon.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to specify dependencies required by an addon.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Dependency {

    /**
     * Gets the ID of the dependency.
     *
     * @return The dependency ID.
     */
    String id();

    /**
     * Checks whether this dependency is optional. Optional dependencies are not strictly required for the addon to
     * function, and the addon can work without them if they are not available.
     *
     * @return {@code true} if the dependency is optional; {@code false} otherwise.
     */
    boolean optional() default false;
}

package su.plo.voice.api.addon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Addon {

    /**
     * Returns the addon id
     *
     * @return the addon id
     */
    String id();

    /**
     * Returns the addon name
     * <p></p>
     * Can be translatable
     *
     * @return the addon name
     */
    String name() default "";

    /**
     * Returns the addon scope
     *
     * @return the addon scope
     */
    Scope scope();

    /**
     * Returns the addon version
     *
     * @return the addon version
     */
    String version();

    /**
     * Returns the addon authors
     *
     * @return the addon authors
     */
    String[] authors();

    // todo: addon dependencies
//    /**
//     * Returns the addon dependencies
//     *
//     * @return the addon dependencies
//     */
//    Dependency[] dependencies() default {};

    enum Scope {

        CLIENT,
        SERVER
    }
}

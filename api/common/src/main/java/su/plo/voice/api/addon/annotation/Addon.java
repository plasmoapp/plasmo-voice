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

    // todo: addon dependencies
//    /**
//     * Returns the addon dependencies
//     *
//     * @return the addon dependencies
//     */
//    Dependency[] dependencies() default {};
}

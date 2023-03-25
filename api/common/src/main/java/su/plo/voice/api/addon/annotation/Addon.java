package su.plo.voice.api.addon.annotation;

import su.plo.voice.api.addon.AddonLoaderScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Addon {

    /***
     * @return the addon id
     */
    String id();

    /**
     * Can be translatable key
     *
     * @return the addon name
     */
    String name() default "";

    /**
     * @return the addon scope
     */
    AddonLoaderScope scope();

    /**
     * @return the addon version
     */
    String version();

    /**
     * @return the addon license, default is MIT
     */
    String license() default "MIT";

    /**
     * @return the addon authors
     */
    String[] authors();

    /**
     * Returns the addon dependencies
     *
     * @return the addon dependencies
     */
    Dependency[] dependencies() default {};
}

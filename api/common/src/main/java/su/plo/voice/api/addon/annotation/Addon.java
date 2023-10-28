package su.plo.voice.api.addon.annotation;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonLoaderScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation used to describe a Plasmo Voice addon.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Addon {

    /**
     * Gets the id of the addon.
     *
     * @return The addon id.
     */
    @NotNull String id();

    /**
     * Gets the name of the addon. This name can be a translatable key.
     *
     * @return The addon name.
     */
    @NotNull String name() default "";


    /**
     * Gets the scope of the addon, indicating where it should be loaded.
     * <br>
     * This is only used if you are using gradle plugin for generating addons' entrypoints.
     *
     * @return The addon scope.
     */
    @NotNull AddonLoaderScope scope() default AddonLoaderScope.ANY;

    /**
     * Gets the version of the addon.
     *
     * @return The addon version.
     */
    @NotNull String version();

    /**
     * Gets the authors of the addon.
     *
     * @return The addon authors.
     */
    @NotNull String[] authors();

    /**
     * Gets the dependencies required or suggested by the addon.
     *
     * @return The addon dependencies.
     */
    Dependency[] dependencies() default {};
}

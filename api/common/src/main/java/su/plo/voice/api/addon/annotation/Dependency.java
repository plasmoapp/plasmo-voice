package su.plo.voice.api.addon.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Dependency {

    /**
     * Returns the dependency id
     * @return the dependency id
     */
    String id();

    /**
     * Returns whether this dependency is optional
     * @return true if dependency is optional
     */
    boolean optional() default false;
}

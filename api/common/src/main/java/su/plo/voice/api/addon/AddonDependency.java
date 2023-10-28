package su.plo.voice.api.addon;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a dependency on another addon.
 */
@EqualsAndHashCode
@ToString
public final class AddonDependency {

    @Getter
    private final String id;
    @Getter
    private final boolean optional;

    /**
     * Creates a new addon dependency.
     *
     * @param id       The addon id.
     * @param optional Whether this dependency is optional.
     */
    public AddonDependency(@NonNull String id, boolean optional) {
        this.id = id;
        checkArgument(!id.isEmpty(), "id cannot be empty");
        this.optional = optional;
    }
}

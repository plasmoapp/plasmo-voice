package su.plo.voice.api.addon;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a dependency on another addon
 */
public final class AddonDependency {

    @Getter
    private final String id;
    @Getter
    private final boolean optional;

    /**
     * Creates a new addon dependency
     *
     * @param id       the addon id
     * @param optional whether this dependency is optional
     */
    public AddonDependency(String id, boolean optional) {
        this.id = checkNotNull(id, "id cannot be null");
        checkArgument(!id.isEmpty(), "id cannot be empty");
        this.optional = optional;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddonDependency that = (AddonDependency) o;
        return optional == that.optional
                && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, optional);
    }

    @Override
    public String toString() {
        return "AddonDependency{"
                + "id='" + id + '\''
                + ", optional=" + optional
                + '}';
    }
}

package su.plo.voice.api.addon;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a dependency on another addon
 */
public final class AddonDependency {

    @Getter
    private final String id;
    @Getter
    private final boolean optional;
    @Getter
    private final boolean mod;

    /**
     * Creates a new addon dependency
     *
     * @param id       the addon id
     * @param optional whether this dependency is optional
     * @param mod      whether this dependency is mod dependency
     */
    public AddonDependency(@NonNull String id, boolean optional, boolean mod) {
        this.id = id;
        checkArgument(!id.isEmpty(), "id cannot be empty");
        this.optional = optional;
        this.mod = mod;
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

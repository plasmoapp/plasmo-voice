package su.plo.voice.api.addon.annotation.processor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.AddonDependency;
import su.plo.voice.api.addon.AddonScope;
import su.plo.voice.api.addon.annotation.Addon;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Data
public final class JsonAddon {
    private static final Gson GSON = new Gson();
    private static Type LIST_TYPE = new TypeToken<List<JsonAddon>>() {
    }.getType();

    private final String id;
    private final String name;
    private final AddonScope scope;
    private final String version;
    private final List<String> authors;
    private final List<AddonDependency> dependencies;
    private final String mainClass;

    private JsonAddon(@NonNull String id,
                      @NonNull String name,
                      @NonNull AddonScope scope,
                      @NonNull String version,
                      @NonNull List<String> authors,
                      @NonNull List<AddonDependency> dependencies,
                      @NonNull String mainClass) {
        this.id = id;
        this.name = name;
        this.scope = scope;
        this.version = version;
        this.authors = authors;
        this.dependencies = dependencies;
        checkArgument(!id.isEmpty(), "id cannot be empty");
        checkArgument(AddonContainer.ID_PATTERN.matcher(id).matches(), "id is not valid");
        this.mainClass = mainClass;
    }

    static JsonAddon from(Addon addon, String mainClass) {
        return new JsonAddon(
                addon.id(),
                addon.name(),
                addon.scope(),
                addon.version(),
                Lists.newArrayList(addon.authors()),
                Arrays.stream(addon.dependencies())
                        .map(dependency -> new AddonDependency(dependency.id(), dependency.optional()))
                        .collect(Collectors.toList()),
                mainClass
        );
    }

    public static List<JsonAddon> from(Reader json) {
        return GSON.fromJson(json, LIST_TYPE);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonAddon that = (JsonAddon) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "JsonAddon{"
                + "id='" + id + '\''
                + ", main=" + mainClass
                + '}';
    }
}

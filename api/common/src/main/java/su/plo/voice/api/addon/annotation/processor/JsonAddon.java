package su.plo.voice.api.addon.annotation.processor;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.annotation.Addon;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Data
public final class JsonAddon {
    private static final Gson GSON = new Gson();
    private static Type LIST_TYPE = new TypeToken<List<JsonAddon>>() {
    }.getType();

    private final String id;
    private final Addon.Scope scope;
    private final String version;
    private final String[] authors;
    private final String mainClass;

    private JsonAddon(String id, Addon.Scope scope, String version, String[] authors, String mainClass) {
        this.id = checkNotNull(id, "id cannot be null");
        this.scope = checkNotNull(scope, "scope cannot be null");
        this.version = checkNotNull(version, "version cannot be null");
        this.authors = checkNotNull(authors, "authors cannot be null");
        checkArgument(!id.isEmpty(), "id cannot be empty");
        checkArgument(AddonContainer.ID_PATTERN.matcher(id).matches(), "id is not valid");
        this.mainClass = checkNotNull(mainClass, "mainClass cannot be null");
    }

    static JsonAddon from(Addon addon, String qualifiedName) {
        return new JsonAddon(
                addon.id(),
                addon.scope(),
                addon.version(),
                addon.authors(),
                qualifiedName
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

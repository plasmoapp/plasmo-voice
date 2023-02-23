package su.plo.voice.addon;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.AddonDependency;
import su.plo.voice.api.addon.AddonScope;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class VoiceAddon implements AddonContainer {

    @Getter
    private final String id;
    @Getter
    private final String name;
    @Getter
    private final AddonScope scope;
    @Getter
    private final String version;
    @Getter
    private final List<String> authors;
    @Getter
    private final List<AddonDependency> dependencies;
    @Getter
    private final Path path;
    @Getter
    private final Class<?> mainClass;

    private @Nullable Object object;

    public VoiceAddon(@NonNull String id,
                      @NonNull String name,
                      @NonNull AddonScope scope,
                      @NonNull String version,
                      @NonNull List<String> authors,
                      @NonNull List<AddonDependency> dependencies,
                      @NonNull Path path,
                      @NonNull Class<?> mainClass) {
        this.id = id;
        this.name = name;
        this.scope = scope;
        this.version = version;
        this.authors = authors;
        this.dependencies = dependencies;
        this.path = path;
        this.mainClass = mainClass;
    }

    @Override
    public Optional<?> getInstance() {
        return Optional.ofNullable(object);
    }

    public void setInstance(Object object) {
        this.object = object;
    }
}

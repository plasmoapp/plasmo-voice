package su.plo.voice.addon;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.annotation.Addon;

import java.nio.file.Path;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceAddon implements AddonContainer {

    @Getter
    private final String id;
    @Getter
    private final Addon.Scope scope;
    @Getter
    private final String version;
    @Getter
    private final String[] authors;
    @Getter
    private final Path path;
    @Getter
    private final Class<?> mainClass;

    private @Nullable Object object;

    public VoiceAddon(String id,
                      Addon.Scope scope,
                      String version,
                      String[] authors,
                      Path path,
                      Class<?> mainClass) {
        this.id = checkNotNull(id, "id cannot be null");
        this.scope = checkNotNull(scope, "scope cannot be null");
        this.version = checkNotNull(version, "version cannot be null");
        this.authors = checkNotNull(authors, "authors cannot be null");
        this.path = checkNotNull(path, "path cannot be null");
        this.mainClass = checkNotNull(mainClass, "mainClass cannot be null");
    }

    @Override
    public Optional<?> getInstance() {
        return Optional.ofNullable(object);
    }

    public void setInstance(Object object) {
        this.object = object;
    }
}

package su.plo.voice.util.version;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
public enum ModrinthLoader {

    FABRIC("fabric"),
    FORGE("forge"),
    VELOCITY("velocity"),
    BUNGEECORD("bungeecord"),
    PAPER("paper");

    @Getter
    private final String loader;

    ModrinthLoader(@NotNull String loader) {
        this.loader = loader;
    }


    @Override
    public String toString() {
        return loader;
    }
}

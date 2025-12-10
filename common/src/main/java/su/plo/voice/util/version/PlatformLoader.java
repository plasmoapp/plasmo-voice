package su.plo.voice.util.version;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
@Getter
public enum PlatformLoader {

    FABRIC("fabric", true),
    FORGE("forge", true),
    NEO_FORGE("neoforge", true),
    VELOCITY("velocity", true),
    BUNGEECORD("bungeecord", true),
    PAPER("paper", true),
    MINESTOM("minestom", false),
    CUSTOM("custom", false);

    private final String loader;
    private final Boolean modrinthSupported;

    PlatformLoader(@NotNull String loader, boolean modrinthSupported) {
        this.loader = loader;
        this.modrinthSupported = modrinthSupported;
    }


    @Override
    public String toString() {
        return loader;
    }
}

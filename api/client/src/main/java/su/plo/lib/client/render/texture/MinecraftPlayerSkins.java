package su.plo.lib.client.render.texture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MinecraftPlayerSkins {

    CompletableFuture<String> loadSkin(@NotNull UUID playerId, @NotNull String nick, @Nullable String fallback);

    @NotNull String getSkin(@NotNull UUID playerId, @NotNull String nick);

    default @NotNull String getSteveSkin() {
        return "textures/entity/steve.png";
    }

    default @NotNull String getAlexSkin() {
        return "textures/entity/alex.png";
    }
}

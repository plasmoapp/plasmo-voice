package su.plo.lib.api.client.render.texture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.UUID;

public interface MinecraftPlayerSkins {

    void loadSkin(@NotNull UUID playerId, @NotNull String nick, @Nullable String fallback);

    void loadSkin(@NotNull MinecraftGameProfile gameProfile);

    @NotNull String getSkin(@NotNull UUID playerId, @NotNull String nick);

    @NotNull String getDefaultSkin(@NotNull UUID playerId);
}

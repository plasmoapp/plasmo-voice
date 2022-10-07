package su.plo.lib.client.world;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.entity.MinecraftPlayer;

import java.util.Optional;
import java.util.UUID;

public interface MinecraftClientWorld {

    Optional<MinecraftPlayer> getPlayerById(@NotNull UUID playerId);
}

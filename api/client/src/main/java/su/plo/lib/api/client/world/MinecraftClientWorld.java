package su.plo.lib.api.client.world;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.entity.MinecraftEntity;
import su.plo.lib.api.entity.MinecraftPlayerEntity;

import java.util.Optional;
import java.util.UUID;

public interface MinecraftClientWorld {

    Optional<MinecraftPlayerEntity> getPlayerById(@NotNull UUID playerId);

    Optional<MinecraftEntity> getEntityById(int entityId);
}

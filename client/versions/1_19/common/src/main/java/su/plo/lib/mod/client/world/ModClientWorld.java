package su.plo.lib.mod.client.world;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.world.MinecraftClientWorld;
import su.plo.lib.api.entity.MinecraftEntity;
import su.plo.lib.api.entity.MinecraftPlayerEntity;
import su.plo.lib.mod.entity.ModEntity;
import su.plo.lib.mod.entity.ModPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class ModClientWorld implements MinecraftClientWorld {

    @Getter
    private final ClientLevel level;

    // todo: cleanup?
    private final Map<UUID, MinecraftPlayerEntity> playerById = Maps.newConcurrentMap();
    private final Map<Integer, MinecraftEntity> entityById = Maps.newConcurrentMap();

    @Override
    public Optional<MinecraftPlayerEntity> getPlayerById(@NotNull UUID playerId) {
        Player player = level.getPlayerByUUID(playerId);
        if (player == null) {
            playerById.remove(playerId);
            return Optional.empty();
        }

        return Optional.of(playerById.computeIfAbsent(
                playerId,
                (uuid) -> new ModPlayer<>(player)
        ));
    }

    @Override
    public Optional<MinecraftEntity> getEntityById(int entityId) {
        Entity entity = level.getEntity(entityId);
        if (entity == null) {
            entityById.remove(entityId);
            return Optional.empty();
        }

        return Optional.of(entityById.computeIfAbsent(
                entity.getId(),
                (uuid) -> new ModEntity<>(entity)
        ));
    }
}

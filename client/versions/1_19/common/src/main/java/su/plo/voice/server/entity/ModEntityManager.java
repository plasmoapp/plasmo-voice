package su.plo.voice.server.entity;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.entity.EntityManager;
import su.plo.voice.api.server.entity.VoiceEntity;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class ModEntityManager implements EntityManager {

    private final MinecraftServer server;

    @Override
    public Optional<VoiceEntity> getEntity(@NotNull UUID entityId) {

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity != null) return Optional.of(wrap(entity));
        }

        return Optional.empty();
    }

    @Override
    public @NotNull VoiceEntity wrap(@NotNull Object entity) {
        if (!(entity instanceof Entity serverEntity))
            throw new IllegalArgumentException("entity is not " + Entity.class);

        return new ModVoiceEntity(serverEntity);
    }
}

package su.plo.voice.api.server.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface EntityManager {

    Optional<VoiceEntity> getEntity(@NotNull UUID entityId);

    @NotNull VoiceEntity wrap(@NotNull Object entity);
}

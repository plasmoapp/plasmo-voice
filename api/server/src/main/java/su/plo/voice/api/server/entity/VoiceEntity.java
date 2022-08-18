package su.plo.voice.api.server.entity;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.pos.ServerPos3d;

import java.util.UUID;

public interface VoiceEntity {

    /**
     * Gets the entity id
     */
    int getId();

    /**
     * Gets the entity unique id
     */
    @NotNull UUID getUUID();

    /**
     * Gets the backed entity object
     */
    <T> T getObject();

    @NotNull ServerPos3d getPosition();

    @NotNull ServerPos3d getPosition(@NotNull ServerPos3d position);
}

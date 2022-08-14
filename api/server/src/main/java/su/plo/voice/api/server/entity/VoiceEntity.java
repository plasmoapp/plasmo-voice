package su.plo.voice.api.server.entity;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface VoiceEntity {

    /**
     * Gets the entity unique id
     */
    @NotNull UUID getUUID();

    /**
     * Gets the backed entity object
     */
    <T> T getObject();
}

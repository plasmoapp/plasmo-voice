package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.server.entity.VoiceEntity;

public interface ServerEntitySource extends AudioSource {

    @NotNull VoiceEntity getEntity();

    @Override
    default @NotNull Type getType() {
        return Type.ENTITY;
    }
}

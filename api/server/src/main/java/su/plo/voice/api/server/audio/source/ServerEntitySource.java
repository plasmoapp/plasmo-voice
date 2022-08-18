package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.entity.VoiceEntity;

public interface ServerEntitySource extends ServerAudioSource {

    @NotNull VoiceEntity getEntity();
}

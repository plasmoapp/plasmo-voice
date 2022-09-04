package su.plo.voice.api.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.proto.data.source.SourceInfo;

import java.util.Optional;
import java.util.UUID;

public interface ClientSourceManager extends AudioSourceManager<ClientAudioSource<?>> {

    Optional<ClientAudioSource<?>> getSourceById(@NotNull UUID sourceId, boolean request);

    void update(@NotNull SourceInfo sourceInfo);

    void sendSourceInfoRequest(@NotNull UUID sourceId);
}

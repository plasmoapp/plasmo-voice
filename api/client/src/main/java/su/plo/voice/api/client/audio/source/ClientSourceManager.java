package su.plo.voice.api.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.proto.data.source.SourceInfo;

public interface ClientSourceManager extends AudioSourceManager<ClientAudioSource<?>> {

    void create(@NotNull SourceInfo sourceInfo);
}

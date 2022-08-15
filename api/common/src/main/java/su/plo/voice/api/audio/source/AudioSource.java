package su.plo.voice.api.audio.source;


import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.source.SourceInfo;

import java.util.UUID;

// TODO: doc
public interface AudioSource {

    @NotNull UUID getId();

    @NotNull String getCodec();

    @NotNull SourceInfo getInfo();
}

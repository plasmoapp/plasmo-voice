package su.plo.voice.api.audio.source;


import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.source.SourceInfo;

// TODO: doc
public interface AudioSource {

    @NotNull SourceInfo getInfo();
}

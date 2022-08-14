package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;

public interface AudioSourceHandler {

    boolean canProvide();

    byte[] provide20MsAudio();

    @NotNull String getCodec();

    short getDistance();
}

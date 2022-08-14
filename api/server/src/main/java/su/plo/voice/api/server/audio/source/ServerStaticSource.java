package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.server.pos.ServerPos3d;

public interface ServerStaticSource extends AudioSource {

    void setPosition(@NotNull ServerPos3d position);

    void setAngle(int angle);

    int getAngle();

    @Override
    default @NotNull Type getType() {
        return Type.STATIC;
    }
}

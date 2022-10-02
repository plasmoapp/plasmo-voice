package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.world.ServerPos3d;

public interface ServerStaticSource extends ServerAudioSource {

    void setPosition(@NotNull ServerPos3d position);
}

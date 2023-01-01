package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.proto.data.audio.source.StaticSourceInfo;

public interface ServerStaticSource extends ServerPositionalSource<StaticSourceInfo> {

    void setPosition(@NotNull ServerPos3d position);
}

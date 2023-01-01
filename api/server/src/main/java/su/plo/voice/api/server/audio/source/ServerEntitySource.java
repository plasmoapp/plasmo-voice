package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;

public interface ServerEntitySource extends ServerPositionalSource<EntitySourceInfo> {

    @NotNull MinecraftServerEntity getEntity();
}

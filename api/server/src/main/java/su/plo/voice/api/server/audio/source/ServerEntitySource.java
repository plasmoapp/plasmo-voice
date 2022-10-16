package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.entity.MinecraftServerEntity;

public interface ServerEntitySource extends ServerAudioSource {

    @NotNull MinecraftServerEntity getEntity();
}

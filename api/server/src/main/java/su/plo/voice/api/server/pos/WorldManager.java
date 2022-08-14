package su.plo.voice.api.server.pos;

import org.jetbrains.annotations.NotNull;

public interface WorldManager {

    @NotNull VoiceWorld wrap(@NotNull Object serverWorld);
}

package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;

public interface ServerPlayerSource extends ServerAudioSource {

    @NotNull VoicePlayer getPlayer();
}

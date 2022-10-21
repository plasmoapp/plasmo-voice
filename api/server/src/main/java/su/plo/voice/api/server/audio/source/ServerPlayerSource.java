package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;

public interface ServerPlayerSource extends ServerAudioSource<PlayerSourceInfo> {

    @NotNull VoicePlayer getPlayer();
}

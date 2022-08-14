package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;

import java.util.function.Predicate;

public interface ServerAudioSource extends AudioSource {

    @NotNull ServerPos3d getPosition();

    void addFilter(Predicate<VoicePlayer> filter);

    void removeFilter(Predicate<VoicePlayer> filter);
}

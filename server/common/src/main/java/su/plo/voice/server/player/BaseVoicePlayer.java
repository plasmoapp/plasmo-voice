package su.plo.voice.server.player;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoicePlayer;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseVoicePlayer implements VoicePlayer {

    protected final PlasmoVoiceServer voiceServer;

    @Setter
    private PlayerModLoader modLoader;

    @Setter
    private boolean voiceDisabled;
    @Setter
    private boolean microphoneMuted;

    public BaseVoicePlayer(@NotNull PlasmoVoiceServer voiceServer) {
        this.voiceServer = checkNotNull(voiceServer, "voiceServer");
    }

    @Override
    public Optional<PlayerModLoader> getModLoader() {
        return Optional.ofNullable(modLoader);
    }

    @Override
    public boolean isVoiceDisabled() {
        if (!hasVoiceChat()) throw new IllegalStateException("Player is not connected to UDP server");
        return voiceDisabled;
    }

    @Override
    public boolean isMicrophoneMuted() {
        if (!hasVoiceChat()) throw new IllegalStateException("Player is not connected to UDP server");
        return microphoneMuted;
    }
}

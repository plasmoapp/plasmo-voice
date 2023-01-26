package su.plo.voice.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerPlayerSource;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;

public final class VoiceServerPlayerSource
        extends VoiceServerPositionalSource<PlayerSourceInfo>
        implements ServerPlayerSource {

    private final VoiceServerPlayer player;
    private final ServerPos3d playerPosition = new ServerPos3d();

    public VoiceServerPlayerSource(@NotNull PlasmoVoiceServer voiceServer,
                                   @NotNull AddonContainer addon,
                                   @NotNull ServerSourceLine line,
                                   @Nullable String codec,
                                   boolean stereo,
                                   @NotNull VoiceServerPlayer player) {
        super(voiceServer, addon, player.getInstance().getUUID(), line, codec, stereo);

        this.player = player;
        addFilter(this::filterVanish);
    }

    @Override
    public @NotNull VoiceServerPlayer getPlayer() {
        return player;
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return player.getInstance().getServerPosition(playerPosition);
    }

    private boolean filterVanish(@NotNull VoicePlayer player) {
        return
                !player.equals(this.player) &&
                        ((VoiceServerPlayer) player).getInstance().canSee(this.player.getInstance());
    }

    @Override
    public @NotNull PlayerSourceInfo getInfo() {
        return new PlayerSourceInfo(
                addon.getId(),
                id,
                line.getId(),
                (byte) state.get(),
                codec,
                stereo,
                iconVisible,
                angle,
                player.getInfo()
        );
    }
}

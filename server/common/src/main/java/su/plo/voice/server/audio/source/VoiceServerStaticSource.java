package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerStaticSource;
import su.plo.voice.proto.data.audio.source.StaticSourceInfo;

import java.util.UUID;

public final class VoiceServerStaticSource extends BaseServerSource<StaticSourceInfo> implements ServerStaticSource {

    @Getter
    private ServerPos3d position;

    public VoiceServerStaticSource(@NotNull PlasmoVoiceServer voiceServer,
                                   @NotNull AddonContainer addon,
                                   @NotNull ServerSourceLine line,
                                   @Nullable String codec,
                                   boolean stereo,
                                   @NotNull ServerPos3d position) {
        super(voiceServer, addon, UUID.randomUUID(), line, codec, stereo);

        this.position = position;
    }

    @Override
    public @NotNull StaticSourceInfo getInfo() {
        return new StaticSourceInfo(
                addon.getId(),
                id,
                line.getId(),
                (byte) state.get(),
                codec,
                stereo,
                iconVisible,
                angle,
                position.toPosition(),
                position.getLookAngle()
        );
    }

    @Override
    public void setPosition(@NotNull ServerPos3d position) {
        if (!this.position.equals(position)) {
            this.position = position;
            setDirty();
        }
    }
}

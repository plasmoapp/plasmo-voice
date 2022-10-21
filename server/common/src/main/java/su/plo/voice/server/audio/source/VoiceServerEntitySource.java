package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerEntitySource;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;

public final class VoiceServerEntitySource extends BaseServerSource<EntitySourceInfo> implements ServerEntitySource {

    @Getter
    private final MinecraftServerEntity entity;
    private final ServerPos3d playerPosition = new ServerPos3d();

    public VoiceServerEntitySource(@NotNull PlasmoVoiceServer voiceServer,
                                   @NotNull AddonContainer addon,
                                   @NotNull ServerSourceLine line,
                                   @Nullable String codec,
                                   boolean stereo,
                                   @NotNull MinecraftServerEntity entity) {
        super(voiceServer, addon, entity.getUUID(), line, codec, stereo);

        this.entity = entity;
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return entity.getServerPosition(playerPosition);
    }

    @Override
    public @NotNull EntitySourceInfo getInfo() {
        return new EntitySourceInfo(
                addon.getId(),
                id,
                line.getId(),
                (byte) state.get(),
                codec,
                stereo,
                iconVisible,
                angle,
                entity.getId()
        );
    }
}


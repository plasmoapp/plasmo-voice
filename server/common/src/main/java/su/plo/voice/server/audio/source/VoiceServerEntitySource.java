package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerEntitySource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;

public final class VoiceServerEntitySource extends BaseServerSource implements ServerEntitySource {

    @Getter
    private final VoiceEntity entity;
    private final ServerPos3d playerPosition = new ServerPos3d();

    public VoiceServerEntitySource(UdpServerConnectionManager udpConnections,
                                   @NotNull AddonContainer addon,
                                   @NotNull ServerSourceLine line,
                                   @Nullable String codec,
                                   boolean stereo,
                                   @NotNull VoiceEntity entity) {
        super(udpConnections, addon, entity.getUUID(), line, codec, stereo);

        this.entity = entity;
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return entity.getPosition(playerPosition);
    }

    @Override
    public @NotNull SourceInfo getInfo() {
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


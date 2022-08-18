package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.audio.source.ServerEntitySource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.source.EntitySourceInfo;
import su.plo.voice.proto.data.source.SourceInfo;

public final class VoiceServerEntitySource extends BaseServerSource implements ServerEntitySource {

    @Getter
    private final VoiceEntity entity;
    private final ServerPos3d playerPosition = new ServerPos3d();

    public VoiceServerEntitySource(UdpServerConnectionManager udpConnections, @Nullable String codec, @NotNull VoiceEntity entity) {
        super(udpConnections, entity.getUUID(), codec);
        this.entity = entity;
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return entity.getPosition(playerPosition);
    }

    @Override
    public @NotNull SourceInfo getInfo() {
        return new EntitySourceInfo(id, codec, true, angle, entity.getId());
    }
}


package su.plo.voice.server.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.audio.source.ServerStaticSource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.source.SourceInfo;
import su.plo.voice.proto.data.source.StaticSourceInfo;

import java.util.UUID;

public final class VoiceServerStaticSource extends BaseServerSource implements ServerStaticSource {

    @Setter
    @Getter
    private ServerPos3d position;

    public VoiceServerStaticSource(UdpServerConnectionManager udpConnections,
                                   @Nullable String codec,
                                   @NotNull ServerPos3d position) {
        super(udpConnections, UUID.randomUUID(), codec);
        this.position = position;
    }

    @Override
    public @NotNull SourceInfo getInfo() {
        return new StaticSourceInfo(id, codec, true, angle, position.toPosition(), position.getDirection());
    }
}

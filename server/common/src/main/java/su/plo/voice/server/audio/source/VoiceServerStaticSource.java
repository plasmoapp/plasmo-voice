package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerStaticSource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.data.audio.source.StaticSourceInfo;

import java.util.UUID;

public final class VoiceServerStaticSource extends BaseServerSource implements ServerStaticSource {

    @Getter
    private ServerPos3d position;

    public VoiceServerStaticSource(UdpServerConnectionManager udpConnections,
                                   @NotNull AddonContainer addon,
                                   @NotNull ServerSourceLine line,
                                   @Nullable String codec,
                                   boolean stereo,
                                   @NotNull ServerPos3d position) {
        super(udpConnections, addon, UUID.randomUUID(), line, codec, stereo);

        this.position = position;
    }

    @Override
    public @NotNull SourceInfo getInfo() {
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
        this.position = position;
        setDirty();
    }
}

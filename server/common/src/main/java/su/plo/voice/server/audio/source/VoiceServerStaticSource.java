package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.source.ServerStaticSource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.source.SourceInfo;
import su.plo.voice.proto.data.source.StaticSourceInfo;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.udp.cllientbound.SourceAudioPacket;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VoiceServerStaticSource extends BaseServerSource implements ServerStaticSource {

    private final AtomicBoolean dirty = new AtomicBoolean(true);

    @Getter
    private ServerPos3d position;

    public VoiceServerStaticSource(UdpServerConnectionManager udpConnections,
                                   @NotNull AddonContainer addon,
                                   @Nullable String codec,
                                   boolean stereo,
                                   @NotNull ServerPos3d position) {
        super(udpConnections, addon, UUID.randomUUID(), codec, stereo);
        this.position = position;
    }

    @Override
    public @NotNull SourceInfo getInfo() {
        return new StaticSourceInfo(
                addon.getId(),
                id,
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
        dirty.set(true);
        incrementState();
    }

    @Override
    public void sendAudioPacket(SourceAudioPacket packet, short distance) {
        packet.setSourceState((byte) state.get());

        if (dirty.compareAndSet(true, false))
            sendPacket(new SourceInfoPacket(getInfo()), distance);

        super.sendAudioPacket(packet, distance);
    }
}

package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.audio.source.ServerStaticSource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.source.SourceInfo;
import su.plo.voice.proto.data.source.StaticSourceInfo;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.udp.cllientbound.SourceAudioPacket;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class VoiceServerStaticSource extends BaseServerSource implements ServerStaticSource {

    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private final AtomicInteger state = new AtomicInteger(1);

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
        return new StaticSourceInfo(id, (byte) state.get(), codec, true, angle, position.toPosition(), position.getLookAngle());
    }

    @Override
    public void setPosition(@NotNull ServerPos3d position) {
        this.position = position;
        dirty.set(true);
        state.updateAndGet((operand) -> {
            int value = operand + 1;
            return value > Byte.MAX_VALUE ? Byte.MIN_VALUE : value;
        });
    }

    @Override
    public void sendAudioPacket(SourceAudioPacket packet, short distance) {
        packet.setSourceState((byte) state.get());

        if (dirty.compareAndSet(true, false))
            sendPacket(new SourceInfoPacket(getInfo()), distance);

        super.sendAudioPacket(packet, distance);
    }
}

package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.audio.source.AudioSource;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.Optional;
import java.util.UUID;

/**
 * This event is fired when the {@link SourceAudioPacket} is about to send to source listeners
 */
public final class ServerSourceAudioPacketEvent extends EventCancellableBase {

    @Getter
    private final AudioSource source;
    @Getter
    private final SourceAudioPacket packet;
    @Getter
    @Setter
    private short distance;
    private UUID activationId;

    public ServerSourceAudioPacketEvent(@NotNull AudioSource source,
                                        @NotNull SourceAudioPacket packet,
                                        @Nullable UUID activationId) {
        this(source, packet, (short) -1, activationId);
    }

    public ServerSourceAudioPacketEvent(@NotNull AudioSource source,
                                        @NotNull SourceAudioPacket packet,
                                        short distance,
                                        @Nullable UUID activationId) {
        this.source = source;
        this.packet = packet;
        this.distance = distance;
        this.activationId = activationId;
    }

    public Optional<UUID> getActivationId() {
        return Optional.ofNullable(activationId);
    }
}

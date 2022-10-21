package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

/**
 * This event is fired when the {@link SourceAudioPacket} is about to send to source listeners
 */
public final class ServerSourceAudioPacketEvent extends EventCancellableBase {

    @Getter
    private final ServerAudioSource source;
    @Getter
    private final SourceAudioPacket packet;
    @Getter
    @Setter
    private short distance;

    public ServerSourceAudioPacketEvent(@NotNull ServerAudioSource source,
                                        @NotNull SourceAudioPacket packet,
                                        short distance) {
        this.source = source;
        this.packet = packet;
        this.distance = distance;
    }
}

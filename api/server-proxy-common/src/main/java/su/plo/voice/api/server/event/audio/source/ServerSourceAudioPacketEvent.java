package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.audio.capture.PlayerActivationInfo;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

/**
 * This event is fired when the {@link SourceAudioPacket} is about to send to the players.
 */
public final class ServerSourceAudioPacketEvent extends EventCancellableBase {

    @Getter
    private final ServerAudioSource<?> source;
    @Getter
    private final SourceAudioPacket packet;
    @Getter
    @Setter
    private short distance;
    @Getter
    private @Nullable PlayerActivationInfo activationInfo;
    @Getter
    @Setter
    private Result result = Result.IGNORED;

    public ServerSourceAudioPacketEvent(
            @NotNull ServerAudioSource<?> source,
            @NotNull SourceAudioPacket packet,
            @Nullable PlayerActivationInfo activationInfo
    ) {
        this(source, packet, (short) -1, activationInfo);
    }

    public ServerSourceAudioPacketEvent(
            @NotNull ServerAudioSource<?> source,
            @NotNull SourceAudioPacket packet,
            short distance,
            @Nullable PlayerActivationInfo activationInfo
    ) {
        this.source = source;
        this.packet = packet;
        this.distance = distance;
        this.activationInfo = activationInfo;
    }

    /**
     * Packet handling result.
     */
    public enum Result {
        /**
         * If IGNORED, a source packet will be sent to the players normally.
         */
        IGNORED,
        /**
         * If HANDLED, a source packet will not be sent to the players.
         */
        HANDLED
    }
}

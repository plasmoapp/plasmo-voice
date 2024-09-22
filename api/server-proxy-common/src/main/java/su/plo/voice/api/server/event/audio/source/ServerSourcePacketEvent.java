package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.proto.packets.Packet;

/**
 * This event is fired when the tcp {@link Packet} is about to send to the players.
 */
public final class ServerSourcePacketEvent extends EventCancellableBase {

    @Getter
    private final ServerAudioSource<?> source;
    @Getter
    private final Packet<?> packet;
    @Getter
    @Setter
    private short distance;
    @Getter
    @Setter
    private Result result = Result.IGNORED;

    public ServerSourcePacketEvent(@NotNull ServerAudioSource<?> source,
                                   @NotNull Packet<?> packet) {
        this(source, packet, (short) -1);
    }

    public ServerSourcePacketEvent(@NotNull ServerAudioSource<?> source,
                                   @NotNull Packet<?> packet,
                                   short distance) {
        this.source = source;
        this.packet = packet;
        this.distance = distance;
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

package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.pos.Pos3d;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Note: by default, it will send packets to all players with voice chat.
 * If you want to send packets to a specific player,
 * use {@link AudioDirectSource#setPlayers} to set the players supplier
 * and (or) {@link AudioDirectSource#addFilter(Predicate)} and {@link AudioDirectSource#removeFilter(Predicate)} methods to
 * filter players.
 */
public interface AudioDirectSource<P extends VoicePlayer<?>> extends AudioSource<DirectSourceInfo, P> {

    Optional<P> getSender();

    void setSender(@NotNull P player);

    Optional<Pos3d> getRelativePosition();

    void setRelativePosition(@NotNull Pos3d position);

    Optional<Pos3d> getLookAngle();

    void setLookAngle(@NotNull Pos3d position);

    boolean isCameraRelative();

    void setCameraRelative(boolean cameraRelative);

    void setPlayers(@Nullable Supplier<Collection<P>> playersSupplier);

    boolean sendAudioPacket(@NotNull SourceAudioPacket packet, @Nullable UUID activationId);

    default boolean sendAudioPacket(@NotNull SourceAudioPacket packet) {
        return sendAudioPacket(packet, null);
    }

    boolean sendPacket(Packet<?> packet);
}

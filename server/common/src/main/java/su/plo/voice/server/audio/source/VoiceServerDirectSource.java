package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerDirectSource;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.data.pos.Pos3d;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.udp.cllientbound.SourceAudioPacket;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public final class VoiceServerDirectSource extends BaseServerSource implements ServerDirectSource {

    @Getter
    private final VoicePlayer player;

    private VoicePlayer sender;
    private Pos3d relativePosition;
    private Pos3d lookAngle;
    private boolean cameraRelative = true;

    public VoiceServerDirectSource(UdpServerConnectionManager udpConnections,
                                   @NotNull AddonContainer addon,
                                   @NotNull ServerSourceLine line,
                                   @Nullable String codec,
                                   boolean stereo,
                                   @NotNull VoicePlayer player) {
        super(udpConnections, addon, UUID.randomUUID(), line, codec, stereo);
        this.player = player;
    }

    @Override
    public Optional<VoicePlayer> getSender() {
        return Optional.ofNullable(sender);
    }

    @Override
    public void setSender(@NotNull VoicePlayer player) {
        this.sender = player;
        updateSourceInfo();
    }

    @Override
    public Optional<Pos3d> getRelativePosition() {
        return Optional.ofNullable(relativePosition);
    }

    @Override
    public void setRelativePosition(@NotNull Pos3d position) {
        this.relativePosition = position;
        updateSourceInfo();
    }

    @Override
    public Optional<Pos3d> getLookAngle() {
        return Optional.ofNullable(lookAngle);
    }

    @Override
    public void setLookAngle(@NotNull Pos3d position) {
        this.lookAngle = position;
        updateSourceInfo();
    }

    @Override
    public boolean isCameraRelative() {
        return cameraRelative;
    }

    @Override
    public void setCameraRelative(boolean cameraRelative) {
        this.cameraRelative = cameraRelative;
        updateSourceInfo();
    }

    @Override
    public @NotNull SourceInfo getInfo() {
        return new DirectSourceInfo(
                addon.getId(),
                id,
                line.getId(),
                (byte) state.get(),
                codec,
                stereo,
                iconVisible,
                angle,
                sender == null ? null : sender.getUUID(),
                relativePosition,
                lookAngle,
                cameraRelative
        );
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return player.getPosition();
    }

    @Override
    public void addFilter(Predicate<VoicePlayer> filter) {
        throw new IllegalStateException("This source type is not supports filters");
    }

    @Override
    public void removeFilter(Predicate<VoicePlayer> filter) {
        throw new IllegalStateException("This source type is not supports filters");
    }

    @Override
    public void sendAudioPacket(SourceAudioPacket packet, short distance) {
        udpConnections.getConnectionByUUID(player.getUUID())
                .ifPresent(connection -> connection.sendPacket(packet));
    }

    @Override
    public void sendPacket(Packet<?> packet, short distance) {
        player.sendPacket(packet);
    }

    private void updateSourceInfo() {
        player.sendPacket(new SourceInfoPacket(getInfo()));
    }
}


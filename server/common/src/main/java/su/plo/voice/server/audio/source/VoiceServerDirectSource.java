package su.plo.voice.server.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.ServerDirectSource;
import su.plo.voice.api.server.event.audio.source.ServerSourceAudioPacketEvent;
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.pos.Pos3d;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public final class VoiceServerDirectSource extends BaseServerSource<DirectSourceInfo> implements ServerDirectSource {

    @Getter
    private final VoicePlayer player;

    private VoicePlayer sender;
    private Pos3d relativePosition;
    private Pos3d lookAngle;
    private boolean cameraRelative = true;

    public VoiceServerDirectSource(@NotNull PlasmoVoiceServer voiceServer,
                                   @NotNull AddonContainer addon,
                                   @NotNull ServerSourceLine line,
                                   @Nullable String codec,
                                   boolean stereo,
                                   @NotNull VoicePlayer player) {
        super(voiceServer, addon, UUID.randomUUID(), line, codec, stereo);
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
    public @NotNull DirectSourceInfo getInfo() {
        return new DirectSourceInfo(
                addon.getId(),
                id,
                line.getId(),
                (byte) state.get(),
                codec,
                stereo,
                iconVisible,
                angle,
                sender == null ? null : sender.getInstance().getUUID(),
                relativePosition,
                lookAngle,
                cameraRelative
        );
    }

    @Override
    public @NotNull ServerPos3d getPosition() {
        return player.getInstance().getServerPosition();
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
    public boolean sendAudioPacket(@NotNull SourceAudioPacket packet, short distance, @Nullable UUID activationId) {
        ServerSourceAudioPacketEvent event = new ServerSourceAudioPacketEvent(this, packet, distance, activationId);
        if (!voiceServer.getEventBus().call(event)) return false;

        voiceServer.getUdpConnectionManager()
                .getConnectionByUUID(player.getInstance().getUUID())
                .ifPresent(connection -> connection.sendPacket(packet));
        return true;
    }

    @Override
    public boolean sendPacket(Packet<?> packet, short distance) {
        ServerSourcePacketEvent event = new ServerSourcePacketEvent(this, packet, distance);
        if (!voiceServer.getEventBus().call(event)) return false;

        player.sendPacket(packet);
        return true;
    }

    private void updateSourceInfo() {
        player.sendPacket(new SourceInfoPacket(getInfo()));
    }
}


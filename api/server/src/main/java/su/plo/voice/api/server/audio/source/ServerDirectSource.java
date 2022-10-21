package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.Optional;

public interface ServerDirectSource extends ServerAudioSource<DirectSourceInfo> {

    Optional<VoicePlayer> getSender();

    void setSender(@NotNull VoicePlayer player);

    Optional<Pos3d> getRelativePosition();

    void setRelativePosition(@NotNull Pos3d position);

    Optional<Pos3d> getLookAngle();

    void setLookAngle(@NotNull Pos3d position);

    boolean isCameraRelative();

    void setCameraRelative(boolean cameraRelative);

    @NotNull VoicePlayer getPlayer();
}

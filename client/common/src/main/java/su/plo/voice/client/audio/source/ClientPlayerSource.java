package su.plo.voice.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.entity.MinecraftPlayer;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.Optional;

public final class ClientPlayerSource extends BaseClientAudioSource<PlayerSourceInfo> {

    public ClientPlayerSource(@NotNull MinecraftClientLib minecraft,
                              @NotNull PlasmoVoiceClient voiceClient,
                              @NotNull ClientConfig config,
                              @NotNull SoundOcclusionSupplier soundOcclusionSupplier) {
        super(minecraft, voiceClient, config, soundOcclusionSupplier);
    }

    @Override
    protected float[] getPosition(float[] position) {
        getSourcePlayer().ifPresent((player) -> {
            Pos3d playerPosition = player.getPosition();

            position[0] = (float) playerPosition.getX();
            position[1] = (float) (playerPosition.getY() + player.getEyeHeight());
            position[2] = (float) playerPosition.getZ();
        });

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        getSourcePlayer().ifPresent((player) -> {
            Pos3d playerLookAngle = player.getLookAngle();

            lookAngle[0] = (float) playerLookAngle.getX();
            lookAngle[1] = (float) playerLookAngle.getY();
            lookAngle[2] = (float) playerLookAngle.getZ();
        });

        return lookAngle;
    }

    private Optional<MinecraftPlayer> getSourcePlayer() {
        return minecraft.getWorld().flatMap(world -> world.getPlayerById(sourceInfo.getPlayerInfo().getPlayerId()));
    }
}

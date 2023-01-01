package su.plo.voice.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.entity.MinecraftEntity;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.Optional;

public final class ClientEntitySource extends BaseClientAudioSource<EntitySourceInfo> {

    public ClientEntitySource(@NotNull MinecraftClientLib minecraft,
                              @NotNull PlasmoVoiceClient voiceClient,
                              @NotNull ClientConfig config,
                              @NotNull SoundOcclusionSupplier soundOcclusionSupplier) {
        super(minecraft, voiceClient, config, soundOcclusionSupplier);
    }

    @Override
    protected float[] getPosition(float[] position) {
        getSourceEntity().ifPresent((entity) -> {
            Pos3d entityPosition = entity.getPosition();

            position[0] = (float) entityPosition.getX();
            position[1] = (float) (entityPosition.getY() + entity.getEyeHeight());
            position[2] = (float) entityPosition.getZ();
        });

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        getSourceEntity().ifPresent((entity) -> {
            Pos3d entityLookAngle = entity.getLookAngle();

            lookAngle[0] = (float) entityLookAngle.getX();
            lookAngle[1] = (float) entityLookAngle.getY();
            lookAngle[2] = (float) entityLookAngle.getZ();
        });

        return lookAngle;
    }

    private Optional<MinecraftEntity> getSourceEntity() {
        return minecraft.getWorld().flatMap(world -> world.getEntityById(sourceInfo.getEntityId()));
    }
}

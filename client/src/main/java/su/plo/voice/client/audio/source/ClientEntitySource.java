package su.plo.voice.client.audio.source;

import gg.essential.universal.UMinecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;

import java.util.Optional;

public final class ClientEntitySource extends BaseClientAudioSource<EntitySourceInfo> {

    public ClientEntitySource(@NotNull PlasmoVoiceClient voiceClient,
                              @NotNull ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected float[] getPosition(float[] position) {
        getSourceEntity().ifPresent((entity) -> {
            position[0] = (float) entity.getX();
            position[1] = (float) (entity.getY() + entity.getEyeHeight());
            position[2] = (float) entity.getZ();
        });

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        getSourceEntity().ifPresent((entity) -> {
            Vec3 entityLookAngle = entity.getLookAngle();

            lookAngle[0] = (float) entityLookAngle.x;
            lookAngle[1] = (float) entityLookAngle.y;
            lookAngle[2] = (float) entityLookAngle.z;
        });

        return lookAngle;
    }

    private Optional<Entity> getSourceEntity() {
        ClientLevel level = UMinecraft.getWorld();
        if (level == null) return Optional.empty();

        return Optional.ofNullable(UMinecraft.getWorld().getEntity(sourceInfo.getEntityId()));
    }
}

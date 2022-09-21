package su.plo.voice.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;

public final class ModClientEntitySource extends ModClientAudioSource<EntitySourceInfo> {

    private static final Minecraft minecraft = Minecraft.getInstance();

    public ModClientEntitySource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected float[] getPosition(float[] position) {
        Entity entity = getSourceEntity();

        position[0] = (float) entity.getX();
        position[1] = (float) entity.getEyeY();
        position[2] = (float) entity.getZ();

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        Entity entity = getSourceEntity();

        Vec3 entityLookAngle = entity.getLookAngle();

        lookAngle[0] = (float) entityLookAngle.x();
        lookAngle[1] = (float) entityLookAngle.y();
        lookAngle[2] = (float) entityLookAngle.z();

        return lookAngle;
    }

    private Entity getSourceEntity() {
        Entity entity = minecraft.level.getEntity(sourceInfo.getEntityId());
        if (entity == null) throw new IllegalStateException("Entity not found");

        return entity;
    }
}

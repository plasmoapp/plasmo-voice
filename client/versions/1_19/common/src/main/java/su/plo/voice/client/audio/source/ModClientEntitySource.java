package su.plo.voice.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.source.EntitySourceInfo;

public final class ModClientEntitySource extends BaseClientAudioSource<EntitySourceInfo> {

    private static final Minecraft minecraft = Minecraft.getInstance();

    public ModClientEntitySource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected float[] getPosition(float[] position) {
        Entity entity = getSourceEntity();

        position[0] = (float) entity.position().x();
        position[1] = (float) entity.position().y();
        position[2] = (float) entity.position().z();

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        Entity entity = getSourceEntity();

        lookAngle[0] = (float) entity.getLookAngle().x();
        lookAngle[1] = (float) entity.getLookAngle().y();
        lookAngle[2] = (float) entity.getLookAngle().z();

        return lookAngle;
    }

    private Entity getSourceEntity() {
        Entity entity = minecraft.level.getEntity(sourceInfo.getEntityId());
        if (entity == null) throw new IllegalStateException("Entity not found");

        return entity;
    }
}

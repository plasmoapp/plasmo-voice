package su.plo.voice.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.source.PlayerSourceInfo;

public final class ModClientPlayerSource extends ModClientAudioSource<PlayerSourceInfo> {

    private static final Minecraft minecraft = Minecraft.getInstance();

    public ModClientPlayerSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected float[] getPosition(float[] position) {
        Player player = getSourcePlayer();

        position[0] = (float) player.getX();
        position[1] = (float) player.getEyeY();
        position[2] = (float) player.getZ();

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        Player player = getSourcePlayer();

        Vec3 playerLookAngle = player.getLookAngle();

        lookAngle[0] = (float) playerLookAngle.x();
        lookAngle[1] = (float) playerLookAngle.y();
        lookAngle[2] = (float) playerLookAngle.z();

        return lookAngle;
    }

    private Player getSourcePlayer() {
        Player player = minecraft.level.getPlayerByUUID(sourceInfo.getId());
        if (player == null) throw new IllegalStateException("Player not found");

        return player;
    }
}

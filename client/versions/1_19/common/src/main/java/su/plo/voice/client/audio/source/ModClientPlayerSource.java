package su.plo.voice.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.source.PlayerSourceInfo;

public final class ModClientPlayerSource extends BaseClientAudioSource<PlayerSourceInfo> {

    private static final Minecraft minecraft = Minecraft.getInstance();

    public ModClientPlayerSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected float[] getPlayerPosition(float[] position) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return position;

        position[0] = (float) player.getX();
        position[1] = (float) player.getY();
        position[2] = (float) player.getZ();

        return position;
    }

    @Override
    protected float[] getPosition(float[] position) {
        Player player = getSourcePlayer();

        position[0] = (float) player.position().x();
        position[1] = (float) player.position().y();
        position[2] = (float) player.position().z();

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        Player player = getSourcePlayer();

        lookAngle[0] = (float) player.getLookAngle().x();
        lookAngle[1] = (float) player.getLookAngle().y();
        lookAngle[2] = (float) player.getLookAngle().z();

        return lookAngle;
    }

    private Player getSourcePlayer() {
        Player player = minecraft.level.getPlayerByUUID(sourceInfo.getId());
        if (player == null) throw new IllegalStateException("Player not found");

        return player;
    }
}

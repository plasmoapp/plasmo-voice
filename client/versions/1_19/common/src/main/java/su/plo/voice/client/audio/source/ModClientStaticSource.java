package su.plo.voice.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.source.StaticSourceInfo;

public final class ModClientStaticSource extends BaseClientAudioSource<StaticSourceInfo> {

    public ModClientStaticSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
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
        position[0] = (float) sourceInfo.getPosition().getX();
        position[1] = (float) sourceInfo.getPosition().getY();
        position[2] = (float) sourceInfo.getPosition().getZ();

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        lookAngle[0] = (float) sourceInfo.getLookAngle().getX();
        lookAngle[1] = (float) sourceInfo.getLookAngle().getY();
        lookAngle[2] = (float) sourceInfo.getLookAngle().getZ();

        return lookAngle;
    }
}

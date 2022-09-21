package su.plo.voice.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.audio.SoundOcclusion;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.SourceInfo;

public abstract class ModClientAudioSource<T extends SourceInfo> extends BaseClientAudioSource<T> {

    public ModClientAudioSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected double getOccludedPercent(float[] position) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0D;

        return SoundOcclusion.getOccludedPercent(
                player.level,
                new Vec3(position[0], position[1], position[2]),
                player.getEyePosition()
        );
    }

    @Override
    protected float[] getPlayerPosition(float[] position) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return position;

        position[0] = (float) player.getX();
        position[1] = (float) player.getEyeY();
        position[2] = (float) player.getZ();

        return position;
    }
}

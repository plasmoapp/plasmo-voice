package su.plo.voice.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;

public class ModClientDirectSource extends BaseClientDirectSource {

    public ModClientDirectSource(@NotNull PlasmoVoiceClient voiceClient, ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected float[] getAbsoluteSourcePosition(float[] position) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || sourceInfo.getRelativePosition() == null) return position;

        position[0] = (float) (player.position().x() + sourceInfo.getRelativePosition().getX());
        position[1] = (float) (player.position().y() + sourceInfo.getRelativePosition().getY());
        position[2] = (float) (player.position().z() + sourceInfo.getRelativePosition().getZ());

        return position;
    }
}

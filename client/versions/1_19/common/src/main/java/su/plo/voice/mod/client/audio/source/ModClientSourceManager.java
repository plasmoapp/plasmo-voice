package su.plo.voice.mod.client.audio.source;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.audio.source.BaseClientSourceManager;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.mod.client.audio.SoundOcclusion;

public final class ModClientSourceManager extends BaseClientSourceManager {

    public ModClientSourceManager(@NotNull MinecraftClientLib minecraft,
                                  @NotNull BaseVoiceClient voiceClient,
                                  @NotNull ClientConfig config) {
        super(minecraft, voiceClient, config, (position) -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return 0D;

            return SoundOcclusion.getOccludedPercent(
                    player.level,
                    new Vec3(position[0], position[1], position[2]),
                    player.getEyePosition()
            );
        });
    }
}

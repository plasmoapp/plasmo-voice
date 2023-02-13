package su.plo.voice.client.audio.source;

import gg.essential.universal.UMinecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;

import java.util.Optional;

public final class ClientPlayerSource extends BaseClientAudioSource<PlayerSourceInfo> {

    public ClientPlayerSource(@NotNull PlasmoVoiceClient voiceClient,
                              @NotNull ClientConfig config) {
        super(voiceClient, config);
    }

    @Override
    protected float[] getPosition(float[] position) {
        getSourcePlayer().ifPresent((player) -> {
            position[0] = (float) player.getX();
            position[1] = (float) (player.getY() + player.getEyeHeight());
            position[2] = (float) player.getZ();
        });

        return position;
    }

    @Override
    protected float[] getLookAngle(float[] lookAngle) {
        getSourcePlayer().ifPresent((player) -> {
            Vec3 playerLookAngle = player.getLookAngle();

            lookAngle[0] = (float) playerLookAngle.x;
            lookAngle[1] = (float) playerLookAngle.y;
            lookAngle[2] = (float) playerLookAngle.z;
        });

        return lookAngle;
    }

    private Optional<Player> getSourcePlayer() {
        ClientLevel level = UMinecraft.getWorld();
        if (level == null) return Optional.empty();

        return Optional.ofNullable(UMinecraft.getWorld().getPlayerByUUID(sourceInfo.getPlayerInfo().getPlayerId()));
    }
}

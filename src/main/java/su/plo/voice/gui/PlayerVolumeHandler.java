package su.plo.voice.gui;

import com.google.common.primitives.Ints;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.utils.Utils;

// 5Head
public class PlayerVolumeHandler {
    @Getter
    private static PlayerEntity focusedPlayer;
    @Getter
    private static long lastScroll;
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static boolean isShow(PlayerEntity player) {
        return focusedPlayer != null &&
                focusedPlayer.getUuid().equals(player.getUuid()) &&
                lastScroll != 0 && System.currentTimeMillis() - lastScroll < 1000;
    }

    public static boolean onButton(int action) {
        if (action == 1) {
            if (client.player != null && VoiceClient.isConnected()) {
                focusedPlayer = Utils.getPlayerBySight(client.player.getEntityWorld(), client.player);
                if (focusedPlayer != null && !VoiceClient.getServerConfig().getClients().contains(focusedPlayer.getUuid())) {
                    focusedPlayer = null;
                }
            }
        } else if (action == 0) {
            focusedPlayer = null;
            lastScroll = 0;
        }

        return false;
    }

    public static boolean onMouseScroll(double vertical) {
        if (focusedPlayer != null) {
            lastScroll = System.currentTimeMillis();
            int currentVolume = (int) Math.round(VoiceClient.getClientConfig().getPlayerVolumes().getOrDefault(focusedPlayer.getUuid(), 1.0D) * 100.0D);
            VoiceClient.getClientConfig().getPlayerVolumes().put(focusedPlayer.getUuid(),
                    Ints.constrainToRange(currentVolume + (vertical > 0 ? 5 : -5), 0, 200) / 100.0D);
            return true;
        }
        return false;
    }
}

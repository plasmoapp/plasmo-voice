package su.plo.voice.client.gui;

import com.google.common.primitives.Ints;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.utils.Utils;

// 5Head
public class PlayerVolumeHandler {
    @Getter
    private static Player focusedPlayer;
    @Getter
    private static long lastScroll;
    private static final Minecraft client = Minecraft.getInstance();

    public static boolean isShow(Player player) {
        return focusedPlayer != null &&
                focusedPlayer.getUUID().equals(player.getUUID()) &&
                lastScroll != 0 && System.currentTimeMillis() - lastScroll < 1000;
    }

    public static void onButton(int action) {
        if (action == 1) {
            if (client.player != null && VoiceClient.isConnected()) {
                focusedPlayer = Utils.getPlayerBySight(client.player.level, client.player);
                if (focusedPlayer != null && !VoiceClient.getServerConfig().getClients().contains(focusedPlayer.getUUID())) {
                    focusedPlayer = null;
                }
            }
        } else if (action == 0) {
            focusedPlayer = null;
            lastScroll = 0;
        }
    }

    public static boolean onMouseScroll(double vertical) {
        if (focusedPlayer != null) {
            lastScroll = System.currentTimeMillis();
            int currentVolume = (int) Math.round(VoiceClient.getClientConfig().getPlayerVolumes().getOrDefault(focusedPlayer.getUUID(), 1.0D) * 100.0D);
            VoiceClient.getClientConfig().getPlayerVolumes().put(focusedPlayer.getUUID(),
                    Ints.constrainToRange(currentVolume + (vertical > 0 ? 5 : -5), 0, 200) / 100.0D);
            return true;
        }
        return false;
    }
}

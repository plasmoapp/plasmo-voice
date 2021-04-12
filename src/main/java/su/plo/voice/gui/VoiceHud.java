package su.plo.voice.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerEntity;
import su.plo.voice.Voice;

public class VoiceHud {
    private final Minecraft client = Minecraft.getInstance();

    public void render() {
        if(Voice.socketUDP == null || Voice.serverConfig == null) {
            return;
        }

        final PlayerEntity player = client.player;
        final IngameGui inGameHud = client.gui;
        final MatrixStack matrixStack = new MatrixStack();

        if (player == null) return;

        if(Voice.socketUDP.ping.timedOut) {
            client.getTextureManager().bind(Voice.MICS);

            inGameHud.blit(matrixStack,
                    (client.getWindow().getGuiScaledWidth()/2)-8,
                    client.getWindow().getGuiScaledHeight()-54,
                    0,
                    16,
                    16,
                    16);
            return;
        }

        if(Voice.serverConfig.mutedClients.containsKey(player.getUUID()) || Voice.muted) {
            client.getTextureManager().bind(Voice.MICS);

            inGameHud.blit(matrixStack,
                    (client.getWindow().getGuiScaledWidth()/2)-8,
                    client.getWindow().getGuiScaledHeight()-54,
                    16,
                    0,
                    16,
                    16);
        } else if(Voice.speaking) {
            client.getTextureManager().bind(Voice.MICS);
            if(Voice.speakingPriority) {
                inGameHud.blit(matrixStack,
                        (client.getWindow().getGuiScaledWidth()/2)-8,
                        client.getWindow().getGuiScaledHeight()-54,
                        16,
                        16,
                        16,
                        16);
            } else {
                inGameHud.blit(matrixStack,
                        (client.getWindow().getGuiScaledWidth()/2)-8,
                        client.getWindow().getGuiScaledHeight()-54,
                        0,
                        0,
                        16,
                        16);
            }
        }
    }
}

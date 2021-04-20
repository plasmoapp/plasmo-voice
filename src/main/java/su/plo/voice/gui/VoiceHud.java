package su.plo.voice.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import su.plo.voice.client.VoiceClient;

public class VoiceHud {
    private final MinecraftClient client;

    public VoiceHud() {
        this.client = MinecraftClient.getInstance();
    }

    public void render() {
        if(VoiceClient.socketUDP == null || VoiceClient.serverConfig == null) {
            return;
        }

        final PlayerEntity player = client.player;
        final InGameHud inGameHud = client.inGameHud;
        final MatrixStack matrixStack = new MatrixStack();

        if (player == null) return;

        if(VoiceClient.socketUDP.ping.timedOut) {
            client.getTextureManager().bindTexture(VoiceClient.MICS);

            inGameHud.drawTexture(matrixStack,
                    (client.getWindow().getScaledWidth()/2)-8,
                    client.getWindow().getScaledHeight()-54,
                    0,
                    16,
                    16,
                    16);
            return;
        }

        if(VoiceClient.serverConfig.mutedClients.containsKey(player.getUuid()) || VoiceClient.muted) {
            client.getTextureManager().bindTexture(VoiceClient.MICS);

            inGameHud.drawTexture(matrixStack,
                    (client.getWindow().getScaledWidth()/2)-8,
                    client.getWindow().getScaledHeight()-54,
                    16,
                    0,
                    16,
                    16);
        } else if(VoiceClient.speaking) {
            client.getTextureManager().bindTexture(VoiceClient.MICS);
            if(VoiceClient.speakingPriority) {
                inGameHud.drawTexture(matrixStack,
                        (client.getWindow().getScaledWidth()/2)-8,
                        client.getWindow().getScaledHeight()-54,
                        16,
                        16,
                        16,
                        16);
            } else {
                inGameHud.drawTexture(matrixStack,
                        (client.getWindow().getScaledWidth()/2)-8,
                        client.getWindow().getScaledHeight()-54,
                        0,
                        0,
                        16,
                        16);
            }
        }
    }
}

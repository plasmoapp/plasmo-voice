package su.plo.voice.gui;

import com.mojang.blaze3d.systems.RenderSystem;
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
        if(!VoiceClient.connected()) {
            return;
        }

        final PlayerEntity player = client.player;
        final InGameHud inGameHud = client.inGameHud;
        final MatrixStack matrixStack = new MatrixStack();

        if (player == null) return;

        if(VoiceClient.socketUDP.ping.timedOut) {
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            client.getTextureManager().bindTexture(VoiceClient.MICS);

            inGameHud.drawTexture(matrixStack,
                    VoiceClient.config.micIconPosition.getX(client),
                    VoiceClient.config.micIconPosition.getY(client),
                    0,
                    16,
                    16,
                    16);
            return;
        }

        if(VoiceClient.serverConfig.mutedClients.containsKey(player.getUuid()) || VoiceClient.muted) {
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            client.getTextureManager().bindTexture(VoiceClient.MICS);

            inGameHud.drawTexture(matrixStack,
                    VoiceClient.config.micIconPosition.getX(client),
                    VoiceClient.config.micIconPosition.getY(client),
                    16,
                    0,
                    16,
                    16);
        } else if(VoiceClient.speaking) {
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            client.getTextureManager().bindTexture(VoiceClient.MICS);

            if(VoiceClient.speakingPriority) {
                inGameHud.drawTexture(matrixStack,
                        VoiceClient.config.micIconPosition.getX(client),
                        VoiceClient.config.micIconPosition.getY(client),
                        16,
                        16,
                        16,
                        16);
            } else {
                inGameHud.drawTexture(matrixStack,
                        VoiceClient.config.micIconPosition.getX(client),
                        VoiceClient.config.micIconPosition.getY(client),
                        0,
                        0,
                        16,
                        16);
            }
        }
    }
}

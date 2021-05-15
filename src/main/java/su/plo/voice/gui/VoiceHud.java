package su.plo.voice.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerEntity;
import su.plo.voice.Voice;

public class VoiceHud {
    private final Minecraft client = Minecraft.getInstance();

    public void render() {
        if(!Voice.connected()) {
            return;
        }

        final PlayerEntity player = client.player;
        final IngameGui inGameHud = client.gui;
        final MatrixStack matrixStack = new MatrixStack();

        if (player == null) return;

        if(Voice.socketUDP.ping.timedOut) {
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            client.getTextureManager().bind(Voice.MICS);

            inGameHud.blit(matrixStack,
                    Voice.config.micIconPosition.getX(client),
                    Voice.config.micIconPosition.getY(client),
                    0,
                    16,
                    16,
                    16);
            return;
        }

        if(Voice.serverConfig.mutedClients.containsKey(player.getUUID()) || Voice.muted) {
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            client.getTextureManager().bind(Voice.MICS);

            inGameHud.blit(matrixStack,
                    Voice.config.micIconPosition.getX(client),
                    Voice.config.micIconPosition.getY(client),
                    16,
                    0,
                    16,
                    16);
        } else if(Voice.speaking) {
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            client.getTextureManager().bind(Voice.MICS);

            if(Voice.speakingPriority) {
                inGameHud.blit(matrixStack,
                        Voice.config.micIconPosition.getX(client),
                        Voice.config.micIconPosition.getY(client),
                        16,
                        16,
                        16,
                        16);
            } else {
                inGameHud.blit(matrixStack,
                        Voice.config.micIconPosition.getX(client),
                        Voice.config.micIconPosition.getY(client),
                        0,
                        0,
                        16,
                        16);
            }
        }
    }
}

package su.plo.voice.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import su.plo.voice.client.VoiceClient;

public class VoiceHud {
    private final Minecraft client = Minecraft.getInstance();;

    public void render() {
        if(!VoiceClient.isConnected()) {
            return;
        }

        final Player player = client.player;
        final Gui inGameHud = client.gui;
        final PoseStack matrixStack = new PoseStack();

        if (player == null) return;

        if(VoiceClient.socketUDP.ping.timedOut) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, VoiceClient.ICONS);

            inGameHud.blit(matrixStack,
                    VoiceClient.getClientConfig().micIconPosition.get().getX(client),
                    VoiceClient.getClientConfig().micIconPosition.get().getY(client),
                    0,
                    16,
                    16,
                    16);
            return;
        }

        if(VoiceClient.getClientConfig().speakerMuted.get()) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, VoiceClient.ICONS);

            inGameHud.blit(matrixStack,
                    VoiceClient.getClientConfig().micIconPosition.get().getX(client),
                    VoiceClient.getClientConfig().micIconPosition.get().getY(client),
                    80,
                    0,
                    16,
                    16);
        } else if(VoiceClient.getClientConfig().microphoneMuted.get() || !VoiceClient.recorder.isAvailable()
                || VoiceClient.getServerConfig().getMuted().containsKey(player.getUUID())) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, VoiceClient.ICONS);

            inGameHud.blit(matrixStack,
                    VoiceClient.getClientConfig().micIconPosition.get().getX(client),
                    VoiceClient.getClientConfig().micIconPosition.get().getY(client),
                    16,
                    0,
                    16,
                    16);
        } else if(VoiceClient.isSpeaking()) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, VoiceClient.ICONS);

            if(VoiceClient.isSpeakingPriority()) {
                inGameHud.blit(matrixStack,
                        VoiceClient.getClientConfig().micIconPosition.get().getX(client),
                        VoiceClient.getClientConfig().micIconPosition.get().getY(client),
                        16,
                        16,
                        16,
                        16);
            } else {
                inGameHud.blit(matrixStack,
                        VoiceClient.getClientConfig().micIconPosition.get().getX(client),
                        VoiceClient.getClientConfig().micIconPosition.get().getY(client),
                        0,
                        0,
                        16,
                        16);
            }
        }
    }
}

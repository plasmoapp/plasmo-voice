package su.plo.voice.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import su.plo.voice.client.VoiceClient;

public class VoiceHud {
    private final Minecraft client = Minecraft.getInstance();

    public void render() {
        if (!VoiceClient.isConnected()) return;

        final Player player = client.player;
        if (player == null) return;

        if (VoiceClient.socketUDP.isTimedOut()) {
            renderConnectionError();
            return;
        }

        if (VoiceClient.getClientConfig().speakerMuted.get()) {
            renderSpeakerMuted();
        } else if (VoiceClient.getClientConfig().microphoneMuted.get()
                || !VoiceClient.recorder.isAvailable()
                || VoiceClient.getServerConfig().getMuted().containsKey(player.getUUID())
        ) {
            renderMicrophoneMuted();
        } else if (VoiceClient.isSpeaking()) {
            if (VoiceClient.isSpeakingPriority()) {
                renderPrioritySpeaking();
            } else {
                renderSpeaking();
            }
        }
    }

    private void renderConnectionError() {
        renderIcon(0, 16);
    }

    private void renderSpeakerMuted() {
        renderIcon(80, 0);
    }

    private void renderMicrophoneMuted() {
        renderIcon(16, 0);
    }

    private void renderSpeaking() {
        renderIcon(0, 0);
    }

    private void renderPrioritySpeaking() {
        renderIcon(16, 16);
    }

    private void renderIcon(int x, int z) {
        final Gui inGameHud = client.gui;
        final PoseStack matrixStack = new PoseStack();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, VoiceClient.ICONS);

        inGameHud.blit(
                matrixStack,
                VoiceClient.getClientConfig().micIconPosition.get().getX(client),
                VoiceClient.getClientConfig().micIconPosition.get().getY(client),
                x,
                z,
                16,
                16
        );
    }
}

package su.plo.voice.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.gui.PlayerVolumeHandler;
import su.plo.voice.socket.SocketClientUDPQueue;

public class CustomEntityRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void entityRender(PlayerEntity player, double distance, MatrixStack matrices, boolean hasLabel, VertexConsumerProvider vertexConsumers, int light) {
        if (VoiceClient.getClientConfig().getShowIcons() == 2) {
            return;
        }

        if (player.getUuid().equals(client.player.getUuid())) {
            return;
        }

        if (!client.player.networkHandler.getPlayerUuids().contains(player.getUuid())) {
            return;
        }

        if (player.isInvisibleTo(MinecraftClient.getInstance().player) ||
                (client.options.hudHidden && VoiceClient.getClientConfig().getShowIcons() == 0)) {
            return;
        }

        if (VoiceClient.getServerConfig().getClients().contains(player.getUuid())) {
            if (VoiceClient.getClientConfig().isMuted(player.getUuid())) {
                renderIcon(VoiceClient.SPEAKER_MUTED, player, distance, matrices, hasLabel, vertexConsumers, light);
            } else if (VoiceClient.getServerConfig().getMuted().containsKey(player.getUuid())) {
                MutedEntity muted = VoiceClient.getServerConfig().getMuted().get(player.getUuid());
                if (muted.to == 0 || muted.to > System.currentTimeMillis()) {
                    renderIcon(VoiceClient.SPEAKER_MUTED, player, distance, matrices, hasLabel, vertexConsumers, light);
                } else {
                    VoiceClient.getServerConfig().getMuted().remove(muted.uuid);
                }
            } else if (SocketClientUDPQueue.talking.containsKey(player.getUuid())) {
                if (SocketClientUDPQueue.talking.get(player.getUuid())) {
                    renderIcon(VoiceClient.SPEAKER_PRIORITY, player, distance, matrices, hasLabel, vertexConsumers, light);
                } else {
                    renderIcon(VoiceClient.SPEAKER_ICON, player, distance, matrices, hasLabel, vertexConsumers, light);
                }
            } else if (PlayerVolumeHandler.isShow(player)) {
                renderPercent(player, distance, matrices, hasLabel, vertexConsumers, light);
            }
        } else {
            renderIcon(VoiceClient.SPEAKER_WARNING, player, distance, matrices, hasLabel, vertexConsumers, light);
        }
    }

    private static void renderPercent(PlayerEntity player, double distance, MatrixStack matrices, boolean hasLabel,
                                      VertexConsumerProvider vertexConsumers, int light) {
        double yOffset = 0.5D;
        if (hasLabel) {
            yOffset += 0.3D;

            Scoreboard scoreboard = player.getScoreboard();
            ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(2);
            if (scoreboardObjective != null && distance < 100.0D) {
                yOffset += 0.3D;
            }
        }

        matrices.push();
        matrices.translate(0D, player.getHeight() + yOffset, 0D);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        matrices.translate(0D, -1D, 0D);

        Matrix4f matrix4f = matrices.peek().getModel();
        boolean bl = !player.isSneaky();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int j = (int) (g * 255.0F) << 24;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        Text text = new LiteralText((int) Math.round(VoiceClient.getClientConfig().getPlayerVolumes().getOrDefault(player.getUuid(), 1.0D) * 100.0D) + "%");

        float h = (float) (-textRenderer.getWidth(text) / 2);
        textRenderer.draw(text, h, 0, 553648127, false, matrix4f, vertexConsumers, bl, j, light);
        if (bl) {
            textRenderer.draw(text, h, 0, -1, false, matrix4f, vertexConsumers, false, 0, light);
        }

        matrices.pop();
    }

    private static void renderIcon(Identifier identifier, PlayerEntity player, double distance, MatrixStack matrices,
                                   boolean hasLabel, VertexConsumerProvider vertexConsumers, int light) {
        double yOffset = 0.5D;
        if (PlayerVolumeHandler.isShow(player)) {
            renderPercent(player, distance, matrices, hasLabel, vertexConsumers, light);
            yOffset += 0.3D;
        }

        if (hasLabel) {
            yOffset += 0.3D;

            Scoreboard scoreboard = player.getScoreboard();
            ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(2);
            if (scoreboardObjective != null && distance < 100.0D) {
                yOffset += 0.3D;
            }
        }

        matrices.push();
        matrices.translate(0D, player.getHeight() + yOffset, 0D);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        matrices.translate(0D, -1D, 0D);

        float offset = -5F;

        VertexConsumer builder = vertexConsumers.getBuffer(RenderLayer.getText(identifier));
        if (player.isDescending()) {
            vertex(builder, matrices, offset, 10F, 0F, 0F, 1F, 40, light);
            vertex(builder, matrices, offset + 10F, 10F, 0F, 1F, 1F, 40, light);
            vertex(builder, matrices, offset + 10F, 0F, 0F, 1F, 0F, 40, light);
            vertex(builder, matrices, offset, 0F, 0F, 0F, 0F, 40, light);
        } else {
            vertex(builder, matrices, offset, 10F, 0F, 0F, 1F, 255, light);
            vertex(builder, matrices, offset + 10F, 10F, 0F, 1F, 1F, 255, light);
            vertex(builder, matrices, offset + 10F, 0F, 0F, 1F, 0F, 255, light);
            vertex(builder, matrices, offset, 0F, 0F, 0F, 0F, 255, light);

            VertexConsumer builderSeeThrough = vertexConsumers.getBuffer(RenderLayer.getTextSeeThrough(identifier));
            vertex(builderSeeThrough, matrices, offset, 10F, 0F, 0F, 1F, 40, light);
            vertex(builderSeeThrough, matrices, offset + 10F, 10F, 0F, 1F, 1F, 40, light);
            vertex(builderSeeThrough, matrices, offset + 10F, 0F, 0F, 1F, 0F, 40, light);
            vertex(builderSeeThrough, matrices, offset, 0F, 0F, 0F, 0F, 40, light);
        }

        matrices.pop();
    }

    private static void vertex(VertexConsumer builder, MatrixStack matrices, float x, float y, float z, float u, float v, int alpha, int light) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f modelViewMatrix = entry.getModel();

        builder.vertex(modelViewMatrix, x, y, z);
        builder.color(255, 255, 255, alpha);
        builder.texture(u, v);
        builder.overlay(OverlayTexture.DEFAULT_UV);
        builder.light(light);
        builder.normal(0F, 0F, -1F);
        builder.next();
    }
}

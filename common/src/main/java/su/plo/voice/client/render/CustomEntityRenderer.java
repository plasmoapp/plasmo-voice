package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.PlayerVolumeHandler;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.common.entities.MutedEntity;

public class CustomEntityRenderer {
    private static final Minecraft client = Minecraft.getInstance();

    public static void entityRender(Player player, double distance, PoseStack matrices, boolean hasLabel, MultiBufferSource vertexConsumers, int light) {
        if (VoiceClient.getClientConfig().showIcons.get() == 2) {
            return;
        }

        if (player.getUUID().equals(client.player.getUUID())) {
            return;
        }

        if (!client.player.connection.getOnlinePlayerIds().contains(player.getUUID())) {
            return;
        }

        if (player.isInvisibleTo(client.player) ||
                (client.options.hideGui && VoiceClient.getClientConfig().showIcons.get() == 0)) {
            return;
        }

        if (VoiceClient.getServerConfig().getClients().contains(player.getUUID())) {
            if (VoiceClient.getClientConfig().isMuted(player.getUUID())) {
                renderIcon(80, 0, player, distance, matrices, hasLabel, vertexConsumers, light);
            } else if (VoiceClient.getServerConfig().getMuted().containsKey(player.getUUID())) {
                MutedEntity muted = VoiceClient.getServerConfig().getMuted().get(player.getUUID());
                if (muted.to == 0 || muted.to > System.currentTimeMillis()) {
                    renderIcon(80, 0, player, distance, matrices, hasLabel, vertexConsumers, light);
                } else {
                    VoiceClient.getServerConfig().getMuted().remove(muted.uuid);
                }
            } else {
                Boolean isTalking = SocketClientUDPQueue.talking.get(player.getUUID());
                if (isTalking != null) {
                    if (isTalking) {
                        renderIcon(96, 0, player, distance, matrices, hasLabel, vertexConsumers, light);
                    } else {
                        renderIcon(64, 0, player, distance, matrices, hasLabel, vertexConsumers, light);
                    }
                } else if (PlayerVolumeHandler.isShow(player)) {
                    renderPercent(player, distance, matrices, hasLabel, vertexConsumers, light);
                }
            }
        } else {
            renderIcon(112, 0, player, distance, matrices, hasLabel, vertexConsumers, light);
        }
    }

    private static void renderPercent(Player player, double distance, PoseStack matrices, boolean hasLabel,
                                      MultiBufferSource vertexConsumers, int light) {
        double yOffset = 0.5D;
        if (hasLabel) {
            yOffset += 0.3D;

            Scoreboard scoreboard = player.getScoreboard();
            Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
            if (scoreboardObjective != null && distance < 100.0D) {
                yOffset += 0.3D;
            }
        }

        matrices.pushPose();
        matrices.translate(0D, player.getBbHeight() + yOffset, 0D);
        matrices.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        matrices.translate(0D, -1D, 0D);

        Matrix4f matrix4f = matrices.last().pose();
        boolean bl = !player.isDescending();
        float g = client.options.getBackgroundOpacity(0.25F);
        int j = (int) (g * 255.0F) << 24;
        Font textRenderer = client.font;

        Component text = new TextComponent((int) Math.round(VoiceClient.getClientConfig().getPlayerVolumes().getOrDefault(player.getUUID(), 1.0D) * 100.0D) + "%");

        float h = (float) (-textRenderer.width(text) / 2);
        textRenderer.drawInBatch(text, h, 0, 553648127, false, matrix4f, vertexConsumers, bl, j, light);
        if (bl) {
            textRenderer.drawInBatch(text, h, 0, -1, false, matrix4f, vertexConsumers, false, 0, light);
        }

        matrices.popPose();
    }

    private static void renderIcon(float u, float v, Player player, double distance, PoseStack matrices,
                                   boolean hasLabel, MultiBufferSource vertexConsumers, int light) {
        double yOffset = 0.5D;
        if (PlayerVolumeHandler.isShow(player)) {
            renderPercent(player, distance, matrices, hasLabel, vertexConsumers, light);
            yOffset += 0.3D;
        }

        if (hasLabel) {
            yOffset += 0.3D;

            Scoreboard scoreboard = player.getScoreboard();
            Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
            if (scoreboardObjective != null && distance < 100.0D) {
                yOffset += 0.3D;
            }
        }

        matrices.pushPose();
        matrices.translate(0D, player.getBbHeight() + yOffset, 0D);
        matrices.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        matrices.translate(0D, -1D, 0D);

        float offset = -5F;

        VertexConsumer builder = vertexConsumers.getBuffer(RenderType.text(VoiceClient.ICONS));

        float u0 = u / (float) 256;
        float u1 = (u + (float) 16) / (float) 256;
        float v0 = v / (float) 256;
        float v1 = (v + (float) 16) / (float) 256;

        if (player.isDescending()) {
            vertex(builder, matrices, offset, 10F, 0F, u0, v1, 40, light);
            vertex(builder, matrices, offset + 10F, 10F, 0F, u1, v1, 40, light);
            vertex(builder, matrices, offset + 10F, 0F, 0F, u1, v0, 40, light);
            vertex(builder, matrices, offset, 0F, 0F, u0, v0, 40, light);
        } else {
            vertex(builder, matrices, offset, 10F, 0F, u0, v1, 255, light);
            vertex(builder, matrices, offset + 10F, 10F, 0F, u1, v1, 255, light);
            vertex(builder, matrices, offset + 10F, 0F, 0F, u1, v0, 255, light);
            vertex(builder, matrices, offset, 0F, 0F, u0, v0, 255, light);

            VertexConsumer builderSeeThrough = vertexConsumers.getBuffer(RenderType.textSeeThrough(VoiceClient.ICONS));
            vertex(builderSeeThrough, matrices, offset, 10F, 0F, u0, v1, 40, light);
            vertex(builderSeeThrough, matrices, offset + 10F, 10F, 0F, u1, v1, 40, light);
            vertex(builderSeeThrough, matrices, offset + 10F, 0F, 0F, u1, v0, 40, light);
            vertex(builderSeeThrough, matrices, offset, 0F, 0F, u0, v0, 40, light);
        }

        matrices.popPose();
    }

    private static void vertex(VertexConsumer builder, PoseStack matrices, float x, float y, float z, float u, float v, int alpha, int light) {
        PoseStack.Pose entry = matrices.last();
        Matrix4f modelViewMatrix = entry.pose();

        builder.vertex(modelViewMatrix, x, y, z);
        builder.color(255, 255, 255, alpha);
        builder.uv(u, v);
        builder.overlayCoords(OverlayTexture.NO_OVERLAY);
        builder.uv2(light);
        builder.normal(0F, 0F, -1F);
        builder.endVertex();
    }
}

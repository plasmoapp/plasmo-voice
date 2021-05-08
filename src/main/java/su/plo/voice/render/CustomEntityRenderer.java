package su.plo.voice.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import su.plo.voice.Voice;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.socket.SocketClientUDPQueue;

public class CustomEntityRenderer {
    private static final Minecraft client = Minecraft.getInstance();

    public static void entityRender(PlayerEntity player, double distance, MatrixStack matrices, boolean hasLabel, IRenderTypeBuffer vertexConsumers, int light) {
        if(Voice.config.showIcons == 2) {
            return;
        }

        if(player.getUUID().equals(client.player.getUUID())) {
            return;
        }

        if(!client.player.connection.getOnlinePlayerIds().contains(player.getUUID())) {
            return;
        }

        if(player.isInvisibleTo(Minecraft.getInstance().player) || (client.options.hideGui && Voice.config.showIcons == 0)) {
            return;
        }

        if(Voice.serverConfig.clients.contains(player.getUUID())) {
            if(Voice.clientMutedClients.contains(player.getUUID())) {
                renderIcon(Voice.SPEAKER_MUTED, player, distance, matrices, hasLabel, vertexConsumers, light);
            } else if (Voice.serverConfig.mutedClients.containsKey(player.getUUID())) {
                MutedEntity muted = Voice.serverConfig.mutedClients.get(player.getUUID());
                if(muted.to == 0 || muted.to > System.currentTimeMillis()) {
                    renderIcon(Voice.SPEAKER_MUTED, player, distance, matrices, hasLabel, vertexConsumers, light);
                } else {
                    Voice.serverConfig.mutedClients.remove(muted.uuid);
                }
            } else if(SocketClientUDPQueue.talking.containsKey(player.getUUID())) {
                if(SocketClientUDPQueue.talking.get(player.getUUID())) {
                    renderIcon(Voice.SPEAKER_PRIORITY, player, distance, matrices, hasLabel, vertexConsumers, light);
                } else {
                    renderIcon(Voice.SPEAKER_ICON, player, distance, matrices, hasLabel, vertexConsumers, light);
                }
            }
        } else {
            renderIcon(Voice.SPEAKER_WARNING, player, distance, matrices, hasLabel, vertexConsumers, light);
        }
    }

    private static void renderIcon(ResourceLocation identifier, PlayerEntity player, double distance, MatrixStack matrices, boolean hasLabel,
                                   IRenderTypeBuffer vertexConsumers, int light) {

        double yOffset = 0.5D;
        if (hasLabel) {
            yOffset += 0.3D;

            Scoreboard scoreboard = player.getScoreboard();
            ScoreObjective scoreboardObjective = scoreboard.getDisplayObjective(2);
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

        IVertexBuilder builder = vertexConsumers.getBuffer(RenderType.text(identifier));
        if(player.isDescending()) {
            vertex(builder, matrices, offset, 10F, 0F, 0F, 1F, 40, light);
            vertex(builder, matrices, offset + 10F, 10F, 0F, 1F, 1F, 40, light);
            vertex(builder, matrices, offset + 10F, 0F, 0F, 1F, 0F, 40, light);
            vertex(builder, matrices, offset, 0F, 0F, 0F, 0F, 40, light);
        } else {
            vertex(builder, matrices, offset, 10F, 0F, 0F, 1F, 255, light);
            vertex(builder, matrices, offset + 10F, 10F, 0F, 1F, 1F, 255, light);
            vertex(builder, matrices, offset + 10F, 0F, 0F, 1F, 0F, 255, light);
            vertex(builder, matrices, offset, 0F, 0F, 0F, 0F, 255, light);

            IVertexBuilder builderSeeThrough  = vertexConsumers.getBuffer(RenderType.textSeeThrough(identifier));
            vertex(builderSeeThrough, matrices, offset, 10F, 0F, 0F, 1F, 40, light);
            vertex(builderSeeThrough, matrices, offset + 10F, 10F, 0F, 1F, 1F, 40, light);
            vertex(builderSeeThrough, matrices, offset + 10F, 0F, 0F, 1F, 0F, 40, light);
            vertex(builderSeeThrough, matrices, offset, 0F, 0F, 0F, 0F, 40, light);
        }

        matrices.popPose();
    }

    private static void vertex(IVertexBuilder builder, MatrixStack matrices, float x, float y, float z, float u, float v, int alpha, int light) {
        MatrixStack.Entry entry = matrices.last();
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

package su.plo.voice.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.audio.source.ModClientStaticSource;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public final class SourceIconRenderer {

    private static SourceIconRenderer instance;

    public static SourceIconRenderer getInstance() {
        if (instance == null) {
            instance = new SourceIconRenderer(
                    ModVoiceClient.INSTANCE.getAudioCapture(),
                    ModVoiceClient.INSTANCE.getSourceManager(),
                    ModVoiceClient.INSTANCE.getConfig()
            );
        }

        return instance;
    }

    private final Minecraft minecraft = Minecraft.getInstance();
    private final BufferBuilder bufferBuilder = new BufferBuilder(2097152);
    private VertexBuffer vertexBuffer;

    private final AudioCapture capture;
    private final ClientSourceManager sources;
    private final ClientConfig config;

    // cache resource locations to avoid unnecessary allocations on render
    private final Map<String, ResourceLocation> cachedIconLocations = Maps.newHashMap();

    public void renderEntity(@NotNull Entity entity,
                             double distance,
                             boolean hasLabel,
                             @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource multiBufferSource,
                             int light) {
        Optional<ClientAudioSource<?>> source = sources.getSourceById(entity.getUUID(), false);
        if (source.isEmpty()) return;

        LocalPlayer player = minecraft.player;
        if (player == null) return;

        if (isIconHidden()
                || player.getUUID().equals(entity.getUUID())
                || (entity instanceof Player && !player.connection.getOnlinePlayerIds().contains(entity.getUUID()))
                || entity.isInvisibleTo(player)
        ) return;

        // todo: mute check

        if (!source.get().isActivated()) return;

        // get activation source icon
//        Optional<ClientActivation> activation = capture.getActivationById(source.get().getInfo().getActivation());
//        if (activation.isEmpty()) return;
//
//        ResourceLocation iconLocation = getSourceIconLocation(activation.get());
//        renderIcon(iconLocation, entity, distance, hasLabel, poseStack, multiBufferSource, light);
    }

    public void renderStatics(PoseStack poseStack, Camera camera, Matrix4f matrix4f) {
        for (ClientAudioSource<?> source : sources.getSources()) {
            if (!(source instanceof ModClientStaticSource staticSource)
                    || !source.isActivated()) continue;

            // get activation source icon
//            Optional<ClientActivation> activation = capture.getActivationById(source.getInfo().getActivation());
//            if (activation.isEmpty()) return;
//
//            if (isIconHidden()) return;
//
//            ResourceLocation iconLocation = getSourceIconLocation(activation.get());
//            renderStatic(iconLocation, staticSource.getInfo().getPosition(), poseStack, camera, matrix4f);
        }
    }

    private void renderStatic(ResourceLocation iconLocation, Pos3d position, PoseStack poseStack, Camera camera, Matrix4f matrix4f) {
        Vec3 pos = new Vec3(position.getX(), position.getY(), position.getZ());
        Vec3 cameraPos = camera.getPosition();

        if (cameraPos.distanceToSqr(pos) > 4096.0D) return;

        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, iconLocation);

        poseStack.pushPose();

        poseStack.translate(pos.x() - cameraPos.x(), pos.y() - cameraPos.y(), pos.z() - cameraPos.z());
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        poseStack.translate(-5D, 0D, 0D);

        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        vertices(bufferBuilder, null, 255, 15);

        if (vertexBuffer == null) this.vertexBuffer = new VertexBuffer();
        vertexBuffer.bind();
        vertexBuffer.upload(bufferBuilder.end());

        vertexBuffer.drawWithShader(poseStack.last().pose(), matrix4f, GameRenderer.getPositionColorTexLightmapShader());

        poseStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(true);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
    }

    private void renderIcon(@NotNull ResourceLocation iconLocation,
                            @NotNull Entity entity,
                            double distance,
                            boolean hasLabel,
                            @NotNull PoseStack poseStack,
                            @NotNull MultiBufferSource multiBufferSource,
                            int light) {
        double yOffset = 0.5D;
        /* todo: player volumes
         if (PlayerVolumeHandler.isShow(player)) {
            renderPercent(player, distance, matrices, hasLabel, vertexConsumers, light);
            yOffset += 0.3D;
        }
         */

        if (hasLabel) {
            yOffset += 0.3D;

            if (entity instanceof Player player) {
                Scoreboard scoreboard = player.getScoreboard();
                Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
                if (scoreboardObjective != null && distance < 100.0D) {
                    yOffset += 0.3D;
                }
            }
        }

        poseStack.pushPose();
        poseStack.translate(0D, entity.getBbHeight() + yOffset, 0D);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        poseStack.translate(-5D, -1D, 0D);

        VertexConsumer builder = multiBufferSource.getBuffer(RenderType.text(iconLocation));

        if (entity.isDescending()) {
            vertices(builder, poseStack, 40, light);
        } else {
            vertices(builder, poseStack, 255, light);

            VertexConsumer builderSeeThrough = multiBufferSource.getBuffer(RenderType.textSeeThrough(iconLocation));
            vertices(builderSeeThrough, poseStack, 40, light);
        }

        poseStack.popPose();
    }

    private void vertices(VertexConsumer builder, @Nullable PoseStack poseStack, int alpha, int light) {
        vertex(builder, poseStack, 0F, 10F, 0F, 0F, 1F, alpha, light);
        vertex(builder, poseStack, 10F, 10F, 0F, 1F, 1F, alpha, light);
        vertex(builder, poseStack, 10F, 0F, 0F, 1F, 0F, alpha, light);
        vertex(builder, poseStack, 0F, 0F, 0F, 0F, 0F, alpha, light);
    }

    private void vertex(VertexConsumer builder, @Nullable PoseStack poseStack, float x, float y, float z, float u, float v, int alpha, int light) {
        if (poseStack != null) {
            PoseStack.Pose entry = poseStack.last();
            Matrix4f modelViewMatrix = entry.pose();

            builder.vertex(modelViewMatrix, x, y, z);
        } else {
            builder.vertex(x, y, z);
        }

        builder.color(255, 255, 255, alpha);
        builder.uv(u, v);
        builder.overlayCoords(OverlayTexture.NO_OVERLAY);
        builder.uv2(light);
        builder.normal(0F, 0F, -1F);
        builder.endVertex();
    }

    private ResourceLocation getSourceIconLocation(ClientActivation activation) {
        return cachedIconLocations.computeIfAbsent(
                activation.getIcon(),
                ResourceLocation::new
        );
    }

    private boolean isIconHidden() {
        int showIcons = config.getAdvanced().getShowIcons().value();
        return showIcons == 2 || (minecraft.options.hideGui && showIcons == 0);

    }
}

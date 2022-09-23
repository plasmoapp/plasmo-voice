package su.plo.voice.client.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.entity.MinecraftClientPlayer;
import su.plo.lib.client.entity.MinecraftEntity;
import su.plo.lib.client.entity.MinecraftPlayer;
import su.plo.lib.client.event.render.LevelRenderEvent;
import su.plo.lib.client.event.render.PlayerRenderEvent;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.render.MinecraftCamera;
import su.plo.lib.client.render.MinecraftMatrix;
import su.plo.lib.client.render.MinecraftTesselator;
import su.plo.lib.client.render.VertexBuilder;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.VoicePlayerInfo;
import su.plo.voice.proto.data.audio.source.StaticSourceInfo;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.Optional;

public final class SourceIconRenderer {

    private final MinecraftClientLib minecraft;
    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    public SourceIconRenderer(@NotNull MinecraftClientLib minecraft,
                              @NotNull PlasmoVoiceClient voiceClient,
                              @NotNull ClientConfig config) {
        this.minecraft = minecraft;
        this.voiceClient = voiceClient;
        this.config = config;
    }

    @EventSubscribe
    public void onLevelRender(@NotNull LevelRenderEvent event) {
        if (isIconHidden()) return;

        for (ClientAudioSource<?> source : voiceClient.getSourceManager().getSources()) {
            if (!(source.getInfo() instanceof StaticSourceInfo)
                    || !source.isActivated()) continue;

            Optional<ClientSourceLine> sourceLine = voiceClient.getSourceLineManager()
                    .getLineById(source.getInfo().getLineId());
            if (!sourceLine.isPresent()) return;

            renderStatic(
                    event.getRender(),
                    event.getCamera(),
                    sourceLine.get().getIcon(),
                    ((StaticSourceInfo) source.getInfo()).getPosition()
            );
        }
    }

    @EventSubscribe
    public void onEntityRender(@NotNull PlayerRenderEvent event) {
        Optional<ServerConnection> connection = voiceClient.getServerConnection();
        if (!connection.isPresent()) return;

        MinecraftPlayer player = event.getPlayer();

        Optional<MinecraftClientPlayer> clientPlayer = minecraft.getClientPlayer();
        if (!clientPlayer.isPresent()) return;

        if (isIconHidden()
                || player.getUUID().equals(clientPlayer.get().getUUID()) // todo: configurable?
                || event.isFakePlayer()
                || player.isInvisibleTo(clientPlayer.get())
        ) return;

        String iconLocation;

        Optional<VoicePlayerInfo> playerInfo = connection.get().getPlayerById(player.getUUID());
        if (!playerInfo.isPresent()) { // not installed
            iconLocation = "plasmovoice:textures/icons/headset_not_installed.png";
        } else if (config.getVoice().getVolumes().getMute("source_" + player.getUUID()).value()) { // client mute
            iconLocation = "plasmovoice:textures/icons/speaker_disabled.png";
        } else if (playerInfo.get().isMuted()) { // server mute
            iconLocation = "plasmovoice:textures/icons/speaker_muted.png";
        } else if (playerInfo.get().isVoiceDisabled()) { // client disabled voicechat
            iconLocation = "plasmovoice:textures/icons/headset_disabled.png";
        } else {
            Optional<ClientAudioSource<?>> source = voiceClient.getSourceManager()
                    .getSourceById(player.getUUID(), false);
            if (!source.isPresent() || !source.get().isActivated()) return;

            Optional<ClientSourceLine> sourceLine = voiceClient.getSourceLineManager()
                    .getLineById(source.get().getInfo().getLineId());
            if (!sourceLine.isPresent()) return;

            // speaking
            iconLocation = sourceLine.get().getIcon();
        }

        renderEntity(
                event.getRender(),
                event.getCamera(),
                player,
                iconLocation,
                event.hasLabel()
        );
    }

    public void renderEntity(@NotNull GuiRender render,
                             @NotNull MinecraftCamera camera,
                             @NotNull MinecraftEntity entity,
                             @NotNull String iconLocation,
                             boolean hasLabel) {
        Pos3d position = entity.getPosition();

        Pos3d cameraPos = camera.getPosition();
        double distance = cameraPos.distanceSquared(position);
        if (distance > 4096D) return;

        double yOffset = 0.5D;

        /* todo: player volumes
         if (PlayerVolumeHandler.isShow(player)) {
            renderPercent(player, distance, matrices, hasLabel, vertexConsumers, light);
            yOffset += 0.3D;
        }
         */

        if (hasLabel) {
            yOffset += 0.3D;

            if (entity instanceof MinecraftPlayer) {
                MinecraftPlayer player = (MinecraftPlayer) entity;

                if (player.hasLabelScoreboard() && distance < 100D) {
                    yOffset += 0.3D;
                }
            }
        }

        render.turnOnLightLayer();
        render.depthMask(false);
        render.enableBlend();
        render.setShaderTexture(0, iconLocation);
        render.setShaderColor(1F, 1F, 1F, 1F);

        MinecraftMatrix matrix = render.getMatrix();
        MinecraftTesselator tesselator = render.getTesselator();
        VertexBuilder bufferBuilder = tesselator.getBuilder();

        matrix.push();
        matrix.translate(0D, entity.getHitBoxHeight() + yOffset, 0D);
        matrix.multiply(camera.getRotation());
        matrix.scale(-0.025F, -0.025F, 0.025F);
        matrix.translate(-5D, -1D, 0D);

        if (entity.isSneaking()) {
            render.disableDepthTest();
            vertices(render, tesselator, bufferBuilder, matrix, 40, 15);
        } else {
            render.enableBlend();
            render.disableDepthTest();
            vertices(render, tesselator, bufferBuilder, matrix, 40, 15);

            render.disableBlend();
            render.enableDepthTest();
            render.depthMask(true);
            vertices(render, tesselator, bufferBuilder, matrix, 255, 15);
        }

        matrix.pop();

        render.enableDepthTest();
        render.depthFunc(515);
        render.depthMask(true);
        render.turnOffLightLayer();
    }

    private void renderStatic(@NotNull GuiRender render,
                              @NotNull MinecraftCamera camera,
                              @NotNull String iconLocation,
                              @NotNull Pos3d position) {
        Pos3d cameraPos = camera.getPosition();
        if (cameraPos.distanceSquared(position) > 4096D) return;

        render.turnOnLightLayer();
        render.depthMask(false);
        render.enableBlend();
        render.setShaderTexture(0, iconLocation);
        render.setShaderColor(1F, 1F, 1F, 1F);

        MinecraftMatrix matrix = render.getMatrix();
        MinecraftTesselator tesselator = render.getTesselator();
        VertexBuilder bufferBuilder = tesselator.getBuilder();

        matrix.push();
        matrix.translate(
                position.getX() - cameraPos.getX(),
                position.getY() - cameraPos.getY(),
                position.getZ() - cameraPos.getZ()
        );
        matrix.multiply(camera.getRotation());
        matrix.scale(-0.025F, -0.025F, 0.025F);
        matrix.translate(-5D, 0D, 0D);

        render.disableDepthTest();
        vertices(render, tesselator, bufferBuilder, matrix, 40, 15);

        render.enableDepthTest();
        render.depthMask(true);
        vertices(render, tesselator, bufferBuilder, matrix, 255, 15);

        matrix.pop();

        render.enableDepthTest();
        render.depthFunc(515);
        render.turnOffLightLayer();
    }

    private void vertices(@NotNull GuiRender render,
                          @NotNull MinecraftTesselator tesselator,
                          @NotNull VertexBuilder bufferBuilder,
                          @Nullable MinecraftMatrix matrix,
                          int alpha,
                          int light) {
        render.setShader(VertexBuilder.Shader.POSITION_COLOR_TEX_LIGHTMAP);
        bufferBuilder.begin(VertexBuilder.Mode.QUADS, VertexBuilder.Format.POSITION_COLOR_TEX_LIGHTMAP);

        vertex(bufferBuilder, matrix, 0F, 10F, 0F, 0F, 1F, alpha, light);
        vertex(bufferBuilder, matrix, 10F, 10F, 0F, 1F, 1F, alpha, light);
        vertex(bufferBuilder, matrix, 10F, 0F, 0F, 1F, 0F, alpha, light);
        vertex(bufferBuilder, matrix, 0F, 0F, 0F, 0F, 0F, alpha, light);

        tesselator.end();
    }

    private void vertex(VertexBuilder builder, @Nullable MinecraftMatrix matrix, float x, float y, float z, float u, float v, int alpha, int light) {
        if (matrix != null) {
            builder.vertex(matrix, x, y, z);
        } else {
            builder.vertex(x, y, z);
        }

        builder.color(255, 255, 255, alpha);
        builder.uv(u, v);
        builder.overlayCoords(0, 10);
        builder.uv2(light);
        if (matrix != null) {
            builder.normal(matrix, 0F, 0F, -1F);
        } else {
            builder.normal(0F, 0F, -1F);
        }
        builder.endVertex();
    }

    private boolean isIconHidden() {
        int showIcons = config.getAdvanced().getShowIcons().value();
        return showIcons == 2 || (minecraft.getOptions().isGuiHidden() && showIcons == 0);
    }
}

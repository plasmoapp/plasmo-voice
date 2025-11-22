package su.plo.voice.client.render.voice;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.NonNull;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.LazyGlState;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.VertexBuilder;
import su.plo.lib.mod.client.render.entity.LivingEntityRenderState;
import su.plo.lib.mod.client.render.pipeline.RenderPipeline;
import su.plo.lib.mod.client.render.pipeline.RenderPipelines;
import su.plo.lib.mod.extensions.PoseStackKt;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.audio.source.ClientStaticSource;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.event.LivingEntityRenderEvent;
import su.plo.voice.client.event.render.LevelRenderEvent;
import su.plo.voice.client.extension.CameraKt;
import su.plo.voice.client.gui.PlayerVolumeAction;
import su.plo.voice.proto.data.audio.source.EntitySourceInfo;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.data.audio.source.StaticSourceInfo;
import su.plo.voice.proto.data.config.PlayerIconVisibility;
import su.plo.voice.proto.data.player.VoicePlayerInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static su.plo.voice.client.extension.MathKt.toVec3;

//#if MC>=12000
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//#endif

//#if MC>=12103
//$$ import net.minecraft.client.renderer.LightTexture;
//#endif

//#if MC>=12111
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes;
//#endif

public final class SourceIconRenderer {

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;
    private final PlayerVolumeAction volumeAction;

    private final @NotNull LazyGlState glState = new LazyGlState();

    public SourceIconRenderer(
            @NotNull PlasmoVoiceClient voiceClient,
            @NotNull VoiceClientConfig config,
            @NotNull PlayerVolumeAction volumeAction
    ) {
        this.voiceClient = voiceClient;
        this.config = config;
        this.volumeAction = volumeAction;

        LivingEntityRenderEvent.INSTANCE.registerListener(this::onLivingEntityRender);
    }

    @EventSubscribe
    public void onLevelRender(@NotNull LevelRenderEvent event) {
        if (isIconHidden() ||
                !config.getOverlay().getShowStaticSourceIcons().value()
        ) return;

        for (ClientAudioSource<?> source : voiceClient.getSourceManager().getSources()) {
            if (!(source.getSourceInfo() instanceof StaticSourceInfo)
                    || !source.getSourceInfo().isIconVisible()
                    || !source.isActivated()
            ) continue;

            Optional<ClientSourceLine> sourceLine = voiceClient.getSourceLineManager()
                    .getLineById(source.getSourceInfo().getLineId());
            if (!sourceLine.isPresent()) return;

            ClientStaticSource staticSource = (ClientStaticSource) source;
            Pos3d sourcePosition = staticSource.getSourceInfo().getPosition();

            renderStatic(
                    event.getStack(),
                    event.getLightSupplier().getLight(sourcePosition),
                    ResourceLocation.tryParse(sourceLine.get().getIcon()),
                    staticSource,
                    event.getDelta()
            );
        }
    }

    private void onLivingEntityRender(
            @NotNull LivingEntityRenderState entityRenderState,
            @NotNull PoseStack stack,
            int light
    ) {
        if (entityRenderState.getEntityType().equals(EntityType.PLAYER)) {
            renderPlayer(entityRenderState, stack, light);
        } else {
            renderLivingEntity(entityRenderState, stack, light);
        }
    }

    private void renderPlayer(
            @NotNull LivingEntityRenderState entityRenderState,
            @NotNull PoseStack stack,
            int light
    ) {
        Optional<ServerConnection> connection = voiceClient.getServerConnection();
        if (!connection.isPresent()) return;

        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;

        if (entityRenderState.getShouldHideIcon()) return;

        UUID entityUUID = entityRenderState.getEntityUUID();

        boolean isFakePlayer = !Minecraft.getInstance()
                .getConnection()
                .getOnlinePlayerIds()
                .contains(entityUUID);

        Collection<ClientAudioSource<EntitySourceInfo>> entitySources = voiceClient.getSourceManager()
                .getEntitySources(entityRenderState.getEntityId());
        if (isFakePlayer && !entitySources.isEmpty()) {
            renderLivingEntity(entityRenderState, stack, light);
            return;
        }

        if (isIconHidden()
                || entityUUID.equals(clientPlayer.getUUID())
                || isFakePlayer
                || entityRenderState.isInvisibleToPlayer()
        ) return;

        boolean hasPercent = volumeAction.isShown(entityRenderState.getEntityUUID());
        if (hasPercent) {
            renderPercent(
                    entityRenderState,
                    stack,
                    light
            );
        }

        Optional<VoicePlayerInfo> playerInfo = connection.get().getPlayerById(entityUUID);
        String iconLocation = getPlayerIcon(
                playerInfo.orElse(null),
                entityRenderState,
                entityRenderState.getPlayerIconVisibility()
        );
        if (iconLocation == null) return;

        renderEntity(
                entityRenderState,
                stack,
                light,
                ResourceLocation.tryParse(iconLocation),
                hasPercent
        );
    }

    private void renderLivingEntity(
            @NotNull LivingEntityRenderState entityRenderState,
            @NotNull PoseStack stack,
            int light
    ) {
        Optional<ServerConnection> connection = voiceClient.getServerConnection();
        if (!connection.isPresent()) return;

        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;

        if (entityRenderState.getShouldHideIcon()) return;

        if (isIconHidden() || entityRenderState.isInvisibleToPlayer()) return;

        Collection<ClientAudioSource<EntitySourceInfo>> sources = voiceClient.getSourceManager()
                .getEntitySources(entityRenderState.getEntityId());

        ClientSourceLine highestSourceLine = getHighestActivatedSourceLine(sources);
        if (highestSourceLine == null) return;

        renderEntity(
                entityRenderState,
                stack,
                light,
                ResourceLocation.tryParse(highestSourceLine.getIcon()),
                false
        );
    }

    private void renderEntity(
            @NonNull LivingEntityRenderState entityRenderState,
            @NonNull PoseStack stack,
            int light,
            @NonNull ResourceLocation iconLocation,
            boolean hasPercent
    ) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (entityRenderState.getDistanceToCameraSquared() > 4096D) return;

        stack.pushPose();

        if (hasPercent) stack.translate(0D, 0.3D, 0D);
        translateEntityMatrix(entityRenderState, camera, stack);

        if (entityRenderState.isDiscrete()) {
            vertices(stack, 40, light, iconLocation, false);
        } else {
            vertices(stack, 255, light, iconLocation, false);
            vertices(stack, 40, light, iconLocation, true);
        }

        stack.popPose();
    }

    private void renderPercent(
            @NotNull LivingEntityRenderState entityRenderState,
            @NonNull PoseStack stack,
            int light
    ) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (entityRenderState.getDistanceToCameraSquared() > 4096D) return;

        stack.pushPose();

        translateEntityMatrix(entityRenderState, camera, stack);
        stack.translate(5D, 0D, 0D);

        // render percents
        DoubleConfigEntry volume = config.getVoice().getVolumes().getVolume("source_" + entityRenderState.getEntityUUID());

        McTextComponent text = McTextComponent.literal((int) Math.round((volume.value() * 100D)) + "%");
        int backgroundColor = (int) (0.25F * 255.0F) << 24;
        int xOffset = -RenderUtil.getTextWidth(text) / 2;

        glState.withState(() -> {
            RenderUtil.fillLight(
                    stack,
                    RenderPipelines.TEXT_BACKGROUND,
                    xOffset - 1,
                    -1,
                    xOffset + RenderUtil.getTextWidth(text) + 1,
                    8,
                    backgroundColor,
                    light
            );

            RenderUtil.drawStringLight(
                    stack,
                    text,
                    xOffset,
                    0,
                    //#if MC>=12103
                    //$$ Colors.withAlpha(Colors.WHITE, 0.5f).getRGB(),
                    //#else
                    Colors.withAlpha(Colors.WHITE, 0.13f).getRGB(),
                    //#endif
                    light,
                    !entityRenderState.isDiscrete(),
                    false
            );

            RenderUtil.drawStringLight(
                    stack,
                    text,
                    xOffset,
                    0,
                    -1,
                    //#if MC>=12103
                    //$$ LightTexture.lightCoordsWithEmission(light, 2),
                    //#else
                    light,
                    //#endif
                    false,
                    false
            );
        });

        stack.popPose();
    }

    private void translateEntityMatrix(
            @NotNull LivingEntityRenderState entityRenderState,
            @NonNull Camera camera,
            @NonNull PoseStack stack
    ) {
        Vec3 nameTagAttachment = entityRenderState.getNameTagAttachment();

        if (entityRenderState.getNameTag() != null) {
            stack.translate(0D, 0.3D, 0D);

            if (entityRenderState.getHasScoreboardText() && entityRenderState.getDistanceToCameraSquared() < 100D) {
                stack.translate(0D, 0.3D, 0D);
            }
        }

        stack.translate(nameTagAttachment.x(), nameTagAttachment.y() + 0.5D, nameTagAttachment.z());
        PoseStackKt.rotate(stack, -camera.getYRot(), 0.0F, 1.0F, 0.0F);
        PoseStackKt.rotate(stack, camera.getXRot(), 1.0F, 0.0F, 0.0F);
        stack.scale(-0.025F, -0.025F, 0.025F);
        stack.translate(-5D, -1D, 0D);
    }

    private void renderStatic(
            @NonNull PoseStack stack,
            int light,
            @NotNull ResourceLocation iconLocation,
            @NotNull ClientStaticSource staticSource,
            double delta
    ) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        Pos3d position = staticSource.getSourceInfo().getPosition();
        Pos3d lastPosition = staticSource.getLastRenderPosition();

        if (position.distanceSquared(lastPosition) > 1D) {
            lastPosition.setX(position.getX());
            lastPosition.setY(position.getY());
            lastPosition.setZ(position.getZ());
        } else {
            lastPosition.setX(Mth.lerp(delta, lastPosition.getX(), position.getX()));
            lastPosition.setY(Mth.lerp(delta, lastPosition.getY(), position.getY()));
            lastPosition.setZ(Mth.lerp(delta, lastPosition.getZ(), position.getZ()));
        }

        double distanceToCamera = CameraKt.position(camera).distanceToSqr(toVec3(lastPosition));
        if (distanceToCamera > 4096D) return;

        stack.pushPose();

        //#if MC<11800
        //$$ stack.last().pose().setIdentity();
        //$$ stack.last().normal().setIdentity();
        //#endif

        stack.translate(
                lastPosition.getX() - CameraKt.position(camera).x,
                lastPosition.getY() - CameraKt.position(camera).y,
                lastPosition.getZ() - CameraKt.position(camera).z
        );
        PoseStackKt.rotate(stack, -camera.getYRot(), 0.0F, 1.0F, 0.0F);
        PoseStackKt.rotate(stack, camera.getXRot(), 1.0F, 0.0F, 0.0F);
        stack.scale(-0.025F, -0.025F, 0.025F);
        stack.translate(-5D, -5D, 0D);

        vertices(stack, 255, light, iconLocation, false);
        vertices(stack, 40, light, iconLocation, true);

        stack.popPose();
    }

    private void vertices(@NonNull PoseStack stack,
                          int alpha,
                          int light,
                          @NotNull ResourceLocation iconLocation,
                          boolean seeThrough) {
        //#if MC>=12111
        //$$ RenderType renderType = seeThrough
        //$$         ? RenderTypes.textSeeThrough(iconLocation)
        //$$         : RenderTypes.text(iconLocation);
        //#else
        RenderType renderType = seeThrough
                ? RenderType.textSeeThrough(iconLocation)
                : RenderType.text(iconLocation);
        //#endif

        RenderPipeline renderPipeline = RenderPipelines.fromRenderType(
                seeThrough ? "text_see_through" : "text",
                renderType
        );

        BufferBuilder buffer = RenderUtil.beginBuffer(renderPipeline);

        vertex(stack, buffer, 0F, 10F, 0F, 0F, 1F, alpha, light);
        vertex(stack, buffer, 10F, 10F, 0F, 1F, 1F, alpha, light);
        vertex(stack, buffer, 10F, 0F, 0F, 1F, 0F, alpha, light);
        vertex(stack, buffer, 0F, 0F, 0F, 0F, 0F, alpha, light);

        //#if MC>=12100
        //$$ renderType.draw(buffer.build());
        //#elseif MC>=12000
        //$$ renderType.end(buffer, RenderSystem.getVertexSorting());
        //#else
        renderType.end(buffer, 0, 0, 0);
        //#endif
    }

    private void vertex(@NonNull PoseStack stack,
                        @NonNull BufferBuilder buffer,
                        float x, float y, float z, float u, float v, int alpha, int light) {
        VertexBuilder.create(buffer)
                .position(stack, x, y, z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .light(light)
                .normal(stack, 0F, 0F, -1F)
                .end();
    }

    private boolean isIconHidden() {
        if (
                !voiceClient.getServerInfo().isPresent() ||
                !voiceClient.getUdpClientManager().isConnected()
        ) return true;

        int showIcons = config.getOverlay().getShowSourceIcons().value();
        return showIcons == 2 || (Minecraft.getInstance().options.hideGui && showIcons == 0);
    }

    private @Nullable String iconOrNull(
            @NotNull Set<PlayerIconVisibility> iconVisibility,
            @NotNull PlayerIconVisibility targetIconVisibility,
            @NotNull String iconLocation
    ) {
        return iconVisibility.contains(targetIconVisibility)
                ? null
                : iconLocation;
    }

    private @Nullable String getPlayerIcon(
            @Nullable VoicePlayerInfo playerInfo,
            @NotNull LivingEntityRenderState entityRenderState,
            @NotNull Set<PlayerIconVisibility> iconVisibility
    ) {
        // not installed
        if (playerInfo == null) {
            // fallback to entity/player source if we don't have a player in connection list
            String iconBySource = getPlayerIconBySource(entityRenderState, iconVisibility);
            if (iconBySource != null) {
                return iconBySource;
            }

            return iconOrNull(
                    iconVisibility,
                    PlayerIconVisibility.HIDE_NOT_INSTALLED,
                    "plasmovoice:textures/icons/headset_not_installed.png"
            );
        }

        // client mute
        if (config.getVoice().getVolumes().getMute("source_" + playerInfo.getPlayerId()).value()) {
            return iconOrNull(
                    iconVisibility,
                    PlayerIconVisibility.HIDE_CLIENT_MUTED,
                    "plasmovoice:textures/icons/speaker_disabled.png"
            );
        }

        // server mute
        if (playerInfo.isMuted()) {
            return iconOrNull(
                    iconVisibility,
                    PlayerIconVisibility.HIDE_SERVER_MUTED,
                    "plasmovoice:textures/icons/speaker_muted.png"
            );
        }

        // client disabled voicechat
        if (playerInfo.isVoiceDisabled()) {
            return iconOrNull(
                    iconVisibility,
                    PlayerIconVisibility.HIDE_VOICE_CHAT_DISABLED,
                    "plasmovoice:textures/icons/headset_disabled.png"
            );
        }

        return getPlayerIconBySource(entityRenderState, iconVisibility);
    }

    private @Nullable String getPlayerIconBySource(
            @NotNull LivingEntityRenderState entityRenderState,
            @NotNull Set<PlayerIconVisibility> iconVisibility
    ) {
        if (iconVisibility.contains(PlayerIconVisibility.HIDE_SOURCE_ICON)) {
            // nothing to render
            return null;
        }

        Collection<ClientAudioSource<PlayerSourceInfo>> playerSources = voiceClient.getSourceManager()
                .getPlayerSources(entityRenderState.getEntityUUID());
        Collection<ClientAudioSource<EntitySourceInfo>> entitySources = voiceClient.getSourceManager()
                .getEntitySources(entityRenderState.getEntityId());

        if (playerSources.isEmpty() && entitySources.isEmpty()) return null;

        ClientSourceLine highestSourceLine = getHighestActivatedSourceLine(playerSources);
        if (highestSourceLine == null) {
            highestSourceLine = getHighestActivatedSourceLine(entitySources);

            if (highestSourceLine == null) return null;
        }

        return highestSourceLine.getIcon();
    }

    private <T extends SourceInfo> ClientSourceLine getHighestActivatedSourceLine(@NotNull Collection<ClientAudioSource<T>> sources) {
        ClientSourceLine highestSourceLine = null;
        for (ClientAudioSource<?> source : sources) {
            if (!source.isActivated() || !source.getSourceInfo().isIconVisible()) continue;

            Optional<ClientSourceLine> sourceLine = voiceClient.getSourceLineManager()
                    .getLineById(source.getSourceInfo().getLineId());
            if (!sourceLine.isPresent()) continue;

            if (highestSourceLine == null ||
                    highestSourceLine.getWeight() < sourceLine.get().getWeight()
            ) {
                highestSourceLine = sourceLine.get();
            }
        }

        return highestSourceLine;
    }
}

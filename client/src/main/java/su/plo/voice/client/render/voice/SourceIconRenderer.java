package su.plo.voice.client.render.voice;

import net.minecraft.client.renderer.RenderType;
import su.plo.voice.universal.UGraphics;
import su.plo.voice.universal.UMatrixStack;
import su.plo.voice.universal.UMinecraft;
import lombok.NonNull;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.source.ClientAudioSource;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.event.render.EntityRenderEvent;
import su.plo.voice.client.event.render.LevelRenderEvent;
import su.plo.voice.client.event.render.PlayerRenderEvent;
import su.plo.voice.client.gui.PlayerVolumeAction;
import su.plo.voice.client.render.ModCamera;
import su.plo.voice.proto.data.audio.source.StaticSourceInfo;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SourceIconRenderer {

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;
    private final PlayerVolumeAction volumeAction;

    public SourceIconRenderer(@NotNull PlasmoVoiceClient voiceClient,
                              @NotNull VoiceClientConfig config,
                              @NotNull PlayerVolumeAction volumeAction) {
        this.voiceClient = voiceClient;
        this.config = config;
        this.volumeAction = volumeAction;
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

            Pos3d sourcePosition = ((StaticSourceInfo) source.getSourceInfo()).getPosition();

            renderStatic(
                    event.getStack(),
                    event.getCamera(),
                    event.getLightSupplier().getLight(sourcePosition),
                    new ResourceLocation(sourceLine.get().getIcon()),
                    sourcePosition
            );
        }
    }

    @EventSubscribe
    public void onPlayerRender(@NotNull PlayerRenderEvent event) {
        Optional<ServerConnection> connection = voiceClient.getServerConnection();
        if (!connection.isPresent()) return;

        Player player = event.getPlayer();

        LocalPlayer clientPlayer = UMinecraft.getPlayer();
        if (clientPlayer == null) return;

        if (isIconHidden()
                || player.getUUID().equals(clientPlayer.getUUID())
                || event.isFakePlayer()
                || player.isInvisibleTo(clientPlayer)
        ) return;

        boolean hasPercent = false;
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
            Collection<ClientAudioSource<?>> sources = voiceClient.getSourceManager()
                    .getPlayerSources(player.getUUID())
                    .stream()
                    .map(source -> (ClientAudioSource<?>) source) // todo: waytoodank
                    .collect(Collectors.toList());;

            hasPercent = volumeAction.isShown(player);
            if (hasPercent) {
                renderPercent(
                        event.getStack(),
                        event.getCamera(),
                        event.getLight(),
                        player,
                        event.hasLabel()
                );
            }

            if (sources.isEmpty()) return;

            ClientSourceLine highestSourceLine = getHighestActivatedSourceLine(sources);
            if (highestSourceLine == null) return;

            // speaking
            iconLocation = highestSourceLine.getIcon();
        }

        renderEntity(
                event.getStack(),
                event.getCamera(),
                event.getLight(),
                player,
                new ResourceLocation(iconLocation),
                event.hasLabel(),
                hasPercent
        );
    }

    @EventSubscribe
    public void onEntityRender(@NotNull EntityRenderEvent event) {
        Optional<ServerConnection> connection = voiceClient.getServerConnection();
        if (!connection.isPresent()) return;

        Entity entity = event.getEntity();

        LocalPlayer clientPlayer = UMinecraft.getPlayer();
        if (clientPlayer == null) return;

        if (isIconHidden() || entity.isInvisibleTo(clientPlayer)) return;

        Collection<ClientAudioSource<?>> sources = voiceClient.getSourceManager()
                .getEntitySources(entity.getId())
                .stream()
                .map(source -> (ClientAudioSource<?>) source) // todo: waytoodank
                .collect(Collectors.toList());

        ClientSourceLine highestSourceLine = getHighestActivatedSourceLine(sources);
        if (highestSourceLine == null) return;

        renderEntity(
                event.getStack(),
                event.getCamera(),
                event.getLight(),
                entity,
                new ResourceLocation(highestSourceLine.getIcon()),
                event.hasLabel(),
                false
        );
    }

    public void renderEntity(@NonNull UMatrixStack stack,
                             @NonNull ModCamera camera,
                             int light,
                             @NonNull Entity entity,
                             @NonNull ResourceLocation iconLocation,
                             boolean hasLabel,
                             boolean hasPercent) {
        Vec3 position = entity.position();

        double distance = camera.position().distanceToSqr(position);
        if (distance > 4096D) return;

        UGraphics buffer = UGraphics.getFromTessellator();

        stack.push();

        if (hasPercent) stack.translate(0D, 0.3D, 0D);
        translateEntityMatrix(stack, camera, entity, distance, hasLabel);

//        UGraphics.bindTexture(0, iconLocation);
//        UGraphics.color4f(1F, 1F, 1F, 1F);
//        // TRANSLUCENT_TRANSPARENCY
//        UGraphics.enableBlend();
//        UGraphics.tryBlendFuncSeparate(
//                770, // SourceFactor.SRC_ALPHA
//                771, // DestFactor.ONE_MINUS_SRC_ALPHA
//                1, // SourceFactor.ONE
//                771 // DestFactor.ONE_MINUS_SRC_ALPHA
//        );
//        // LIGHT
//        RenderUtil.turnOnLightLayer();

        if (entity.isDescending()) {
            vertices(stack, buffer, 40, light, iconLocation, false);
        } else {
            vertices(stack, buffer, 255, light, iconLocation, false);
            vertices(stack, buffer, 40, light, iconLocation, true);
        }

        stack.pop();

//        // TRANSLUCENT_TRANSPARENCY
//        UGraphics.disableBlend();
//        RenderUtil.defaultBlendFunc();
//        UGraphics.depthMask(true);
//
//        // LIGHT
////        RenderUtil.turnOffLightLayer();
//
//        UGraphics.enableDepth();
//        UGraphics.depthFunc(515);
    }

    private void renderPercent(@NonNull UMatrixStack stack,
                               @NonNull ModCamera camera,
                               int light,
                               @NotNull Entity entity,
                               boolean hasLabel) {
        Vec3 position = entity.position();

        double distance = camera.position().distanceToSqr(position);
        if (distance > 4096D) return;

        stack.push();

        translateEntityMatrix(stack, camera, entity, distance, hasLabel);
        stack.translate(5D, 0D, 0D);

        // render percents
        DoubleConfigEntry volume = config.getVoice().getVolumes().getVolume("source_" + entity.getUUID());

        MinecraftTextComponent text = MinecraftTextComponent.literal((int) Math.round((volume.value() * 100D)) + "%");
        int backgroundColor = (int) (0.25F * 255.0F) << 24;
        int xOffset = -RenderUtil.getTextWidth(text) / 2;

        UGraphics.disableDepth();
        UGraphics.enableBlend();
        UGraphics.depthMask(false);

        RenderUtil.fill(
                stack,
                xOffset - 1,
                -1,
                xOffset + RenderUtil.getTextWidth(text) + 1,
                8,
                backgroundColor
        );
        RenderUtil.drawStringLight(
                stack,
                text,
                xOffset,
                0,
                553648127,
                light,
                !entity.isDescending(),
                false
        );

        UGraphics.enableDepth();
        UGraphics.depthMask(true);
        RenderUtil.drawStringLight(
                stack,
                text,
                xOffset,
                0,
                -1,
                light,
                false,
                false
        );

        UGraphics.disableBlend();
        stack.pop();
    }

    private void translateEntityMatrix(@NonNull UMatrixStack stack,
                                       @NonNull ModCamera camera,
                                       @NotNull Entity entity,
                                       double distance,
                                       boolean hasLabel) {
        if (hasLabel) {
            stack.translate(0D, 0.3D, 0D);

            if (entity instanceof Player) {
                Player player = (Player) entity;

                if (player.getScoreboard().getDisplayObjective(2) != null && distance < 100D) {
                    stack.translate(0D, 0.3D, 0D);
                }
            }
        }

        // todo: legacy getBbHeight?
        stack.translate(0D, entity.getBbHeight() + 0.5D, 0D);
        stack.rotate(-camera.pitch(), 0.0F, 1.0F, 0.0F);
        stack.rotate(camera.yaw(), 1.0F, 0.0F, 0.0F);
        stack.scale(-0.025F, -0.025F, 0.025F);
        stack.translate(-5D, -1D, 0D);
    }

    private void renderStatic(@NonNull UMatrixStack stack,
                              @NonNull ModCamera camera,
                              int light,
                              @NotNull ResourceLocation iconLocation,
                              @NotNull Pos3d position) {
        if (camera.position().distanceToSqr(new Vec3(
                position.getX(), position.getY(), position.getZ()
        )) > 4096D) return;

//        // TEXTURE
//        UGraphics.bindTexture(0, iconLocation);
//        UGraphics.color4f(1F, 1F, 1F, 1F);
//        // TRANSLUCENT_TRANSPARENCY
//        UGraphics.enableBlend();
//        UGraphics.tryBlendFuncSeparate(
//                770, // SourceFactor.SRC_ALPHA
//                771, // DestFactor.ONE_MINUS_SRC_ALPHA
//                1, // SourceFactor.ONE
//                771 // DestFactor.ONE_MINUS_SRC_ALPHA
//        );
//        // LIGHT
//        RenderUtil.turnOnLightLayer();

        UGraphics buffer = UGraphics.getFromTessellator();

        stack.push();
        stack.translate(
                position.getX() - camera.position().x,
                position.getY() - camera.position().y,
                position.getZ() - camera.position().z
        );
        stack.rotate(-camera.pitch(), 0.0F, 1.0F, 0.0F);
        stack.rotate(camera.yaw(), 1.0F, 0.0F, 0.0F);
        stack.scale(-0.025F, -0.025F, 0.025F);
        stack.translate(-5D, 0D, 0D);


        vertices(stack, buffer, 255, light, iconLocation, false);
        vertices(stack, buffer, 40, light, iconLocation, true);

        stack.pop();

//        // TRANSLUCENT_TRANSPARENCY
//        UGraphics.disableBlend();
//        RenderUtil.defaultBlendFunc();
//        UGraphics.depthMask(true);
//
//        // LIGHT
////        RenderUtil.turnOffLightLayer();
//
//        UGraphics.enableDepth();
//        UGraphics.depthFunc(515);
    }

    private void vertices(@NonNull UMatrixStack stack,
                          @NonNull UGraphics buffer,
                          int alpha,
                          int light,
                          @NotNull ResourceLocation iconLocation,
                          boolean seeThrough) {
        if (seeThrough) {
            UGraphics.disableDepth();
            UGraphics.depthMask(false);
        } else {
            UGraphics.enableDepth();
            UGraphics.depthMask(true);
        }

//        //#if MC>=11700
//        if (seeThrough) {
//            UGraphics.setShader(GameRenderer::getRendertypeTextSeeThroughShader);
//        } else {
//            UGraphics.setShader(GameRenderer::getRendertypeTextShader);
//        }
//
//        buffer.beginWithActiveShader(
//                UGraphics.DrawMode.QUADS,
//                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP
//        );
//        //#endif

        //#if MC>=11600
        if (seeThrough) {
            buffer.beginRenderLayer(RenderType.textSeeThrough(iconLocation));
        } else {
            buffer.beginRenderLayer(RenderType.text(iconLocation));
        }
        //#endif

        vertex(stack, buffer, 0F, 10F, 0F, 0F, 1F, alpha, light);
        vertex(stack, buffer, 10F, 10F, 0F, 1F, 1F, alpha, light);
        vertex(stack, buffer, 10F, 0F, 0F, 1F, 0F, alpha, light);
        vertex(stack, buffer, 0F, 0F, 0F, 0F, 0F, alpha, light);

        buffer.drawDirect();
    }

    private void vertex(@NonNull UMatrixStack stack,
                        @NonNull UGraphics buffer,
                        float x, float y, float z, float u, float v, int alpha, int light) {
        buffer.pos(stack, x, y, z);
        buffer.color(255, 255, 255, alpha);
        buffer.tex(u, v);
        buffer.overlay(0, 10);
        buffer.light(light & '\uffff', light >> 16 & '\uffff');
        buffer.norm(stack, 0F, 0F, -1F);

        buffer.endVertex();
    }

    private boolean isIconHidden() {
        int showIcons = config.getOverlay().getShowSourceIcons().value();
        return showIcons == 2 || (UMinecraft.getSettings().hideGui && showIcons == 0);
    }

    private ClientSourceLine getHighestActivatedSourceLine(@NotNull Collection<ClientAudioSource<?>> sources) {
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

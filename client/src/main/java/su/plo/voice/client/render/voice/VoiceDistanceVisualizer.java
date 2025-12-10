package su.plo.voice.client.render.voice;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.VertexBuilder;
import su.plo.lib.mod.client.render.pipeline.RenderPipelines;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.event.audio.capture.ClientActivationRegisteredEvent;
import su.plo.voice.api.client.event.render.VoiceDistanceRenderEvent;
import su.plo.voice.api.client.render.DistanceVisualizer;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.event.render.LevelRenderEvent;
import su.plo.voice.client.extension.CameraKt;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;

import java.util.Map;
import java.util.UUID;

import static su.plo.voice.client.extension.OptionsKt.renderDistanceValue;

@RequiredArgsConstructor
public final class VoiceDistanceVisualizer implements DistanceVisualizer {

    private static final int SPHERE_STACK = 18;
    private static final int SPHERE_SLICE = 36;

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;

    private final Map<UUID, VisualizeEntry> entries = Maps.newConcurrentMap();

//    private int color = 0x00a000;
//    private int alpha = 0;
//    private float radius = 8F;
//    private long lastChanged;

    @Override
    public synchronized void render(int radius, int color, @Nullable Pos3d position) {
        if (!config.getAdvanced().getVisualizeVoiceDistance().value()) return;
        if (radius < 2 || radius > renderDistanceValue(Minecraft.getInstance().options) * 16) return;

        VoiceDistanceRenderEvent event = new VoiceDistanceRenderEvent(this, radius, color);
        if (!voiceClient.getEventBus().fire(event)) return;

        UUID key;
        if (position == null) {
            key = UUID.fromString("00000000-0000-0000-0000-000000000000");
        } else {
            key = UUID.randomUUID();
        }

        entries.put(key, new VisualizeEntry(
                color,
                radius,
                position != null ? new Vec3(position.getX(), position.getY(), position.getZ()) : null
        ));
    }

    @EventSubscribe
    public void onProximityActivationRegistered(@NotNull ClientActivationRegisteredEvent event) {
        ClientActivation activation = event.getActivation();
        if (!activation.getId().equals(VoiceActivation.PROXIMITY_ID)) return;

        boolean isConnected = voiceClient.getUdpClientManager().isConnected();

        if (!isConnected &&
                config.getAdvanced().getVisualizeVoiceDistance().value() &&
                config.getAdvanced().getVisualizeVoiceDistanceOnJoin().value()
        ) {
            voiceClient.getDistanceVisualizer().render(
                    activation.getDistance(),
                    0x00a000,
                    null
            );
        }
    }

    @EventSubscribe
    public void onLevelRender(@NotNull LevelRenderEvent event) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        for (Map.Entry<UUID, VisualizeEntry> entry : entries.entrySet()) {
            UUID key = entry.getKey();
            VisualizeEntry value = entry.getValue();

            if (value.alpha() == 0 || !config.getAdvanced().getVisualizeVoiceDistance().value()) {
                entries.remove(key);
                continue;
            }

            if (value.position() != null &&
                    CameraKt.position(camera).distanceTo(value.position()) > (renderDistanceValue(Minecraft.getInstance().options) * 16)
            ) {
                entries.remove(key);
                continue;
            }

            renderEntry(value, event.getStack(), camera, event.getDelta());
        }
    }

    private void renderEntry(
            @NotNull VisualizeEntry entry,
            @NotNull PoseStack stack,
            @NotNull Camera camera,
            float delta
    ) {
        if (System.currentTimeMillis() - entry.lastChanged() > 2000L) {
            entry.alpha(Math.max(0, entry.alpha() - (10f * delta)));
        }

        Vec3 center;
        if (entry.position() != null) {
            center = entry.position();
        } else {
            LocalPlayer clientPlayer = Minecraft.getInstance().player;
            if (clientPlayer == null) return;

            center = clientPlayer.position();
        }

        stack.pushPose();

        //#if MC<11800
        //$$ stack.last().pose().setIdentity();
        //$$ stack.last().normal().setIdentity();
        //#endif

        stack.translate(
                center.x - CameraKt.position(camera).x,
                center.y - CameraKt.position(camera).y,
                center.z - CameraKt.position(camera).z
        );

        BufferBuilder buffer = RenderUtil.beginBuffer(RenderPipelines.DISTANCE_SPHERE);

        int r = (entry.color() >> 16) & 0xFF;
        int g = (entry.color() >> 8) & 0xFF;
        int b = entry.color() & 0xFF;

        float r0, r1, alpha0, alpha1, x0, x1, y0, y1, z0, z1, beta;
        float stackStep = (float) (Math.PI / SPHERE_STACK);
        float sliceStep = (float) (Math.PI / SPHERE_SLICE);
        for (int i = 0; i < SPHERE_STACK; ++i) {
            alpha0 = (float) (-Math.PI / 2 + i * stackStep);
            alpha1 = alpha0 + stackStep;
            r0 = (float) (entry.radius() * Math.cos(alpha0));
            r1 = (float) (entry.radius() * Math.cos(alpha1));

            y0 = (float) (entry.radius() * Math.sin(alpha0));
            y1 = (float) (entry.radius() * Math.sin(alpha1));

            for (int j = 0; j < (SPHERE_SLICE << 1); ++j) {
                beta = j * sliceStep;
                x0 = (float) (r0 * Math.cos(beta));
                x1 = (float) (r1 * Math.cos(beta));

                z0 = (float) (-r0 * Math.sin(beta));
                z1 = (float) (-r1 * Math.sin(beta));

                VertexBuilder.create(buffer)
                        .position(stack, x0, y0, z0)
                        .color(r, g, b, (int) entry.alpha())
                        .end();
                VertexBuilder.create(buffer)
                        .position(stack, x1, y1, z1)
                        .color(r, g, b, (int) entry.alpha())
                        .end();
            }
        }

        RenderUtil.drawBuffer(buffer, RenderPipelines.DISTANCE_SPHERE);

        stack.popPose();
    }

    @Data
    @Accessors(fluent = true)
    private static final class VisualizeEntry {

        private final int color;
        private final float radius;
        private final @Nullable Vec3 position;

        private float alpha = 150f;
        private long lastChanged = System.currentTimeMillis();
    }
}

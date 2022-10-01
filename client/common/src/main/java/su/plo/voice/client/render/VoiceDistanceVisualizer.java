package su.plo.voice.client.render;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.event.render.LevelRenderEvent;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.render.MinecraftCamera;
import su.plo.lib.client.render.MinecraftTesselator;
import su.plo.lib.client.render.VertexBuilder;
import su.plo.voice.api.client.render.DistanceVisualizer;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.pos.Pos3d;

@RequiredArgsConstructor
public final class VoiceDistanceVisualizer implements DistanceVisualizer {

    private static final int SPHERE_STACK = 18;
    private static final int SPHERE_SLICE = 36;

    private final MinecraftClientLib minecraft;
    private final ClientConfig config;

    private int color = 0x00a000;
    private int alpha = 150;
    private float radius = 8F;
    private long lastChanged;

    @Override
    public void render(int radius, int color) {
        if (!config.getAdvanced().getVisualizeVoiceDistance().value()) return;

        this.color = color;
        this.radius = radius;

        if (radius < 2 || radius > minecraft.getOptions().getRenderDistance() * 16) {
            this.alpha = 0;
            return;
        }

        this.lastChanged = System.currentTimeMillis();
        this.alpha = 150;
    }

    @EventSubscribe
    public void onLevelRender(@NotNull LevelRenderEvent event) {
        render(event.getRender(), event.getCamera(), event.getLightSupplier());
    }

    private void render(@NotNull GuiRender render,
                        @NotNull MinecraftCamera camera,
                        @NotNull LevelRenderEvent.LightSupplier lightSupplier) {
        if (alpha == 0 || !config.getAdvanced().getVisualizeVoiceDistance().value())
            return;

        if (System.currentTimeMillis() - lastChanged > 2000L) {
            alpha -= 5;
        }

        Pos3d cameraPos = camera.getPosition();
        Pos3d center = minecraft.getClientPlayer()
                .orElseThrow(() -> new IllegalStateException("LocalPlayer is empty"))
                .getPosition();

        MinecraftTesselator tesselator = render.getTesselator();
        VertexBuilder bufferBuilder = tesselator.getBuilder();

        // setup render
        render.disableTexture();
        render.disableCull();
        render.enableDepthTest();
        render.depthMask(false);
        render.polygonOffset(-3f, -3f);
        render.enablePolygonOffset();
        render.turnOnLightLayer();
        render.depthFunc(515);

        render.enableBlend();
        render.blendFuncSeparate(
                770, // SourceFactor.SRC_ALPHA
                771, // DestFactor.ONE_MINUS_SRC_ALPHA
                1, // SourceFactor.ONE
                0 // DestFactor.ZERO
        );
        render.setShader(VertexBuilder.Shader.POSITION_COLOR_LIGHTMAP);
        render.setShaderColor(1F, 1F, 1F, 1F);

        render.lineWidth(1f);


        bufferBuilder.begin(VertexBuilder.Mode.TRIANGLE_STRIP, VertexBuilder.Format.POSITION_COLOR_LIGHTMAP);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xF;


        float r0, r1, alpha0, alpha1, x0, x1, y0, y1, z0, z1, beta;
        float stackStep = (float) (Math.PI / SPHERE_STACK);
        float sliceStep = (float) (Math.PI / SPHERE_SLICE);
        for (int i = 0; i < SPHERE_STACK; ++i) {
            alpha0 = (float) (-Math.PI / 2 + i * stackStep);
            alpha1 = alpha0 + stackStep;
            r0 = (float) (radius * Math.cos(alpha0));
            r1 = (float) (radius * Math.cos(alpha1));

            y0 = (float) (radius * Math.sin(alpha0));
            y1 = (float) (radius * Math.sin(alpha1));

            for (int j = 0; j < (SPHERE_SLICE << 1); ++j) {
                beta = j * sliceStep;
                x0 = (float) (r0 * Math.cos(beta));
                x1 = (float) (r1 * Math.cos(beta));

                z0 = (float) (-r0 * Math.sin(beta));
                z1 = (float) (-r1 * Math.sin(beta));

                bufferBuilder.vertex(
                                x0 + center.getX() - cameraPos.getX(),
                                y0 + center.getY() - cameraPos.getY(),
                                z0 + center.getZ() - cameraPos.getZ()
                        )
                        .color(r, g, b, alpha)
                        .uv2(lightSupplier.getLight(new Pos3d(
                                x0 + center.getX() - cameraPos.getX(),
                                y0 + center.getY() - cameraPos.getY(),
                                z0 + center.getZ() - cameraPos.getZ()
                        )))
                        .endVertex();
                bufferBuilder.vertex(
                                x1 + center.getX() - cameraPos.getX(),
                                y1 + center.getY() - cameraPos.getY(),
                                z1 + center.getZ() - cameraPos.getZ()
                        )
                        .color(r, g, b, alpha)
                        .uv2(lightSupplier.getLight(new Pos3d(
                                x1 + center.getX() - cameraPos.getX(),
                                y1 + center.getY() - cameraPos.getY(),
                                z1 + center.getZ() - cameraPos.getZ()
                        )))
                        .endVertex();
            }
        }

        tesselator.end();

        // cleanup render
        render.polygonOffset(0f, 0f);
        render.disablePolygonOffset();
        render.disableBlend();
        render.defaultBlendFunc();
        render.disableDepthTest();
        render.enableCull();
        render.enableTexture();
        render.depthMask(true);
        render.turnOffLightLayer();
    }
}

package su.plo.voice.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import su.plo.voice.api.client.render.DistanceVisualizer;
import su.plo.voice.client.config.ClientConfig;

@RequiredArgsConstructor
public final class VoiceDistanceVisualizer implements DistanceVisualizer {

    private static final int SPHERE_STACK = 18;
    private static final int SPHERE_SLICE = 36;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final ClientConfig config;
    private final BufferBuilder bufferBuilder = new BufferBuilder(2097152);
    private VertexBuffer vertexBuffer;

    private int color = 0x00a000;
    private int alpha = 150;
    private float radius = 8F;
    private long lastChanged;

    @Override
    public void render(int radius, int color) {
        this.color = color;
        this.radius = radius;

        if (radius < 2 || radius > minecraft.options.renderDistance().get()) {
            this.alpha = 0;
            return;
        }

        this.lastChanged = System.currentTimeMillis();
        this.alpha = 150;
    }

    public void render(PoseStack poseStack, Matrix4f matrix4f, Minecraft minecraft) {
        if (alpha == 0 || !config.getAdvanced().getVisualizeVoiceDistance().value())
            return;

        if (System.currentTimeMillis() - lastChanged > 2000L) {
            alpha -= 5;
        }

        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        Vec3 center = minecraft.player.position();

        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

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

                bufferBuilder.vertex(x0 + center.x() - cameraPos.x(), y0 + center.y() - cameraPos.y(), z0 + center.z() - cameraPos.z())
                        .color(r, g, b, alpha)
                        .endVertex();
                bufferBuilder.vertex(x1 + center.x() - cameraPos.x(), y1 + center.y() - cameraPos.y(), z1 + center.z() - cameraPos.z())
                        .color(r, g, b, alpha)
                        .endVertex();
            }
        }

        if (vertexBuffer == null) {
            vertexBuffer = new VertexBuffer();
        }
        vertexBuffer.bind();
        vertexBuffer.upload(bufferBuilder.end());

        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.polygonOffset(-3f, -3f);
        RenderSystem.enablePolygonOffset();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.pushPose();
        RenderSystem.lineWidth(1f);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        vertexBuffer.drawWithShader(poseStack.last().pose(), matrix4f, GameRenderer.getPositionColorShader());

        poseStack.popPose();

        VertexBuffer.unbind();

        RenderSystem.polygonOffset(0f, 0f);
        RenderSystem.disablePolygonOffset();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
    }
}

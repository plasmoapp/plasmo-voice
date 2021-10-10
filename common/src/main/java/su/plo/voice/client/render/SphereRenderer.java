package su.plo.voice.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import su.plo.voice.client.VoiceClient;

public class SphereRenderer {
    private static final SphereRenderer INSTANCE = new SphereRenderer();
    private static final int stack = 18;
    private static final int slice = 36;

    private final BufferBuilder bufferBuilder = new BufferBuilder(2097152);
    private final VertexBuffer vertexBuffer = new VertexBuffer();

    private int alpha = 150;
    private float radius = 8.0F;
    private long lastChanged;
    private boolean render;
    private boolean priority;

    public static SphereRenderer getInstance() {
        return INSTANCE;
    }

    public void setRadius(float radius, boolean priority) {
        setRadius(radius, VoiceClient.getClientConfig().visualizeDistance.get(), priority);
    }

    public void setRadius(float radius, boolean render, boolean priority) {
        this.radius = radius;
        this.priority = priority;

        if (radius > 150 || radius < 2 || !render) {
            this.render = false;
            return;
        }

        this.lastChanged = System.currentTimeMillis();
        this.alpha = 150;
        this.render = true;
    }

    public void render(PoseStack matrices, Matrix4f matrix4f, Minecraft client) {
        if (!render || !VoiceClient.getClientConfig().visualizeDistance.get()) {
            return;
        }

        if (alpha == 0) {
            return;
        }

        if (System.currentTimeMillis() - lastChanged > 2000L) {
            alpha -= 5;
        }

        Vec3 cameraPos = client.gameRenderer.getMainCamera().getPosition();
        Vec3 center = client.player.position();
        
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        int r = this.priority ? 255 : 0;
        int g = this.priority ? 165 : 160;
        int b = 0;


        float r0, r1, alpha0, alpha1, x0, x1, y0, y1, z0, z1, beta;
        float stackStep = (float) (Math.PI / stack);
        float sliceStep = (float) (Math.PI / slice);
        for (int i = 0; i < stack; ++i) {
            alpha0 = (float) (-Math.PI / 2 + i * stackStep);
            alpha1 = alpha0 + stackStep;
            r0 = (float) (radius * Math.cos(alpha0));
            r1 = (float) (radius * Math.cos(alpha1));

            y0 = (float) (radius * Math.sin(alpha0));
            y1 = (float) (radius * Math.sin(alpha1));

            for (int j = 0; j < (slice << 1); ++j) {
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

        bufferBuilder.end();
        vertexBuffer.uploadLater(bufferBuilder);

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

        matrices.pushPose();
        RenderSystem.lineWidth(1f);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        vertexBuffer.drawWithShader(matrices.last().pose(), matrix4f, GameRenderer.getPositionColorShader());

        matrices.popPose();


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

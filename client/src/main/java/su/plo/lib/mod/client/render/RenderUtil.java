package su.plo.lib.mod.client.render;

import net.minecraft.client.gui.Font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.render.pipeline.RenderPipeline;
import su.plo.lib.mod.client.render.pipeline.RenderPipelines;
import su.plo.slib.api.chat.component.McTextComponent;
import lombok.experimental.UtilityClass;
//#if MC<26.2
import net.minecraft.client.renderer.MultiBufferSource;
//#endif
import su.plo.lib.mod.client.chat.ClientTextConverter;

import java.util.Iterator;
import java.util.List;

//#if MC>=12106
//$$ import org.joml.Vector4f;
//#endif

//#if MC>=12105
//$$ import net.minecraft.client.renderer.RenderType;
//$$ import com.mojang.blaze3d.buffers.GpuBuffer;
//$$ import com.mojang.blaze3d.systems.RenderPass;
//$$ import com.mojang.blaze3d.shaders.UniformType;
//$$
//$$ import java.util.OptionalInt;
//$$ import java.util.OptionalDouble;
//#elseif MC>=12102
//$$ import net.minecraft.client.renderer.CoreShaders;
//$$ import net.minecraft.client.renderer.ShaderProgram;
//#endif

@UtilityClass
public class RenderUtil {
    private static final Font.DisplayMode TEXT_LAYER_TYPE = Font.DisplayMode.NORMAL;

    private static final ClientTextConverter TEXT_CONVERTER = new ClientTextConverter();

    //#if MC>=12106
    //$$ private static @Nullable ScissorState SCISSOR_STATE;
    //#endif

    public static void enableScissor(int x, int y, int width, int height) {
        //#if MC>=12106
        //$$ SCISSOR_STATE = new ScissorState(x, y, width, height);
        //#else
        RenderSystem.enableScissor(x, y, width, height);
        //#endif
    }

    public static void disableScissor() {
        //#if MC>=12106
        //$$ SCISSOR_STATE = null;
        //#else
        RenderSystem.disableScissor();
        //#endif
    }

    public static void clearShader() {
        //#if MC>=12105
        //#elseif MC>=12103
        //$$ RenderSystem.clearShader();
        //#else
        RenderSystem.setShader(() -> null);
        //#endif
    }

    public static @NotNull BufferBuilder beginBuffer(@NotNull RenderPipeline pipeline) {
        //#if MC>=26.2
        //$$ throw new UnsupportedOperationException("beginBuffer is no longer supported in 26.2");
        //#else
        Tesselator tesselator = Tesselator.getInstance();
        return tesselator.begin(pipeline.getVertexFormatMode().toMc(), pipeline.getVertexFormat());
        //#endif
    }

    //#if MC<12105
    private static boolean PRESERVE_GL_STATE = false;

    private static @Nullable GlState CURRENT_GL_STATE = null;
    private static @Nullable GlState OLD_GL_STATE = null;

    public static void setGlState(@NotNull GlState glState) {
        OLD_GL_STATE = glState.javaCopy();
        CURRENT_GL_STATE = glState;
        PRESERVE_GL_STATE = true;
    }

    public static void preserveGlState() {
        PRESERVE_GL_STATE = true;
    }

    public static void restoreGlState() {
        restoreGlState(false);
    }

    public static void restoreGlState(boolean preserveGlState) {
        if (OLD_GL_STATE != null && CURRENT_GL_STATE != null) {
            OLD_GL_STATE.apply(CURRENT_GL_STATE);
        }

        CURRENT_GL_STATE = null;
        OLD_GL_STATE = null;
        PRESERVE_GL_STATE = preserveGlState;
    }

    private static void applyRenderPipeline(@NotNull RenderPipeline renderPipeline) {
        //#if MC>=12103
        //$$ RenderSystem.setShader(renderPipeline.getShader().invoke());
        //#else
        RenderSystem.setShader(renderPipeline.getShader()::invoke);
        //#endif

        if (CURRENT_GL_STATE == null) {
            CURRENT_GL_STATE = GlState.current();
            OLD_GL_STATE = CURRENT_GL_STATE.javaCopy();
        }

        renderPipeline.getGlState().apply(CURRENT_GL_STATE);
    }
    //#endif

    public static void drawBuffer(@NotNull BufferBuilder buffer, @NotNull RenderPipeline renderPipeline) {
        //#if MC<=12105
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        //#endif

        //#if MC<12105
        applyRenderPipeline(renderPipeline);
        //#endif


        //#if MC>=26.2
        //$$ throw new UnsupportedOperationException("drawBuffer is no longer supported in 26.2");
        //#elseif MC>=12105
        //$$ try (MeshData meshData = buffer.build()) {
        //$$     renderPipeline.getMcRenderType().draw(meshData);
        //$$ }
        //#else
        BufferUploader.drawWithShader(buffer.build());
        //#endif

        //#if MC<12105
        if (!PRESERVE_GL_STATE) {
            restoreGlState();
        }
        //#endif

        //#if MC>=26.2
        //#else
        clearShader();
        //#endif

    }

    //#if MC<12106
    public static void bindTexture(int index, @NotNull ResourceLocation location) {
        //#if MC>=12105
        //$$ RenderSystem.setShaderTexture(index, Minecraft.getInstance().getTextureManager().getTexture(location).getTexture());
        //#else
        RenderSystem.setShaderTexture(index, location);
        //#endif
    }
    //#endif


    public static void fill(PoseStack stack, int x0, int y0, int x1, int y1, int color) {
        fill(stack, RenderPipelines.GUI_COLOR, x0, y0, x1, y1, color);
    }

    public static void fill(PoseStack stack, @NotNull RenderPipeline renderPipeline, int x0, int y0, int x1, int y1, int color) {
        int n;
        if (x0 < x1) {
            n = x0;
            x0 = x1;
            x1 = n;
        }

        if (y0 < y1) {
            n = y0;
            y0 = y1;
            y1 = n;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float o = (float) (color & 255) / 255.0F;

        BufferBuilder buffer = RenderUtil.beginBuffer(renderPipeline);

        VertexBuilder.create(buffer).position(stack, (float) x0, (float) y1, 0F).color(g, h, o, f).end();
        VertexBuilder.create(buffer).position(stack, (float) x1, (float) y1, 0F).color(g, h, o, f).end();
        VertexBuilder.create(buffer).position(stack, (float) x1, (float) y0, 0F).color(g, h, o, f).end();
        VertexBuilder.create(buffer).position(stack, (float) x0, (float) y0, 0F).color(g, h, o, f).end();

        drawBuffer(buffer, renderPipeline);
    }

    public static void fillLight(PoseStack stack, @NotNull RenderPipeline renderPipeline, int x0, int y0, int x1, int y1, int color, int light) {
        int n;
        if (x0 < x1) {
            n = x0;
            x0 = x1;
            x1 = n;
        }

        if (y0 < y1) {
            n = y0;
            y0 = y1;
            y1 = n;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float o = (float) (color & 255) / 255.0F;

        BufferBuilder buffer = RenderUtil.beginBuffer(renderPipeline);

        VertexBuilder.create(buffer).position(stack, (float) x0, (float) y1, 0F).color(g, h, o, f).light(light).end();
        VertexBuilder.create(buffer).position(stack, (float) x1, (float) y1, 0F).color(g, h, o, f).light(light).end();
        VertexBuilder.create(buffer).position(stack, (float) x1, (float) y0, 0F).color(g, h, o, f).light(light).end();
        VertexBuilder.create(buffer).position(stack, (float) x0, (float) y0, 0F).color(g, h, o, f).light(light).end();

        drawBuffer(buffer, renderPipeline);
    }

    public static void fillGradient(PoseStack stack,
                                    int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z) {
        int f = colorStart >> 24 & 255;
        int g = colorStart >> 16 & 255;
        int h = colorStart >> 8 & 255;
        int i = colorStart & 255;
        int j = colorEnd >> 24 & 255;
        int k = colorEnd >> 16 & 255;
        int l = colorEnd >> 8 & 255;
        int m = colorEnd & 255;

        fillGradient(
                stack,
                startX, startY, endX, endY,
                g, h, i, f,
                k, l, m, j,
                z
        );
    }

    public static void fillGradient(PoseStack stack,
                                    int startX, int startY, int endX, int endY,
                                    int startRed, int startBlue, int startGreen, int startAlpha,
                                    int endRed, int endBlue, int endGreen, int endAlpha,
                                    int z) {
        fillGradientWithPipeline(stack, RenderPipelines.GUI_COLOR, startX, startY, endX, endY, startRed, startBlue, startGreen, startAlpha, endRed, endBlue, endGreen, endAlpha, z);
    }

    public static void fillGradientWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline renderPipeline,
            int startX, int startY, int endX, int endY,
            int startRed, int startBlue, int startGreen, int startAlpha,
            int endRed, int endBlue, int endGreen, int endAlpha,
            int z
    ) {
        BufferBuilder buffer = RenderUtil.beginBuffer(renderPipeline);

        fillGradient(
                stack, buffer, startX, startY, endX, endY, z,
                startRed, startBlue, startGreen, startAlpha,
                endRed, endBlue, endGreen, endAlpha
        );

        drawBuffer(buffer, renderPipeline);
    }

    private static void fillGradient(PoseStack stack, BufferBuilder buffer,
                                     int startX, int startY, int endX, int endY, int z,
                                     int startRed, int startBlue, int startGreen, int startAlpha,
                                     int endRed, int endBlue, int endGreen, int endAlpha) {
        VertexBuilder.create(buffer)
                .position(stack, (float) endX, (float) startY, (float) z)
                .color(startRed, startGreen, startBlue, startAlpha)
                .end();
        VertexBuilder.create(buffer)
                .position(stack, (float) startX, (float) startY, (float) z)
                .color(startRed, startGreen, startBlue, startAlpha)
                .end();
        VertexBuilder.create(buffer)
                .position(stack, (float) startX, (float) endY, (float) z)
                .color(endRed, endGreen, endBlue, endAlpha)
                .end();
        VertexBuilder.create(buffer)
                .position(stack, (float) endX, (float) endY, (float) z)
                .color(endRed, endGreen, endBlue, endAlpha)
                .end();
    }

    public static void blit(PoseStack stack, int x, int y, int u, int v, int width, int height) {
        blit(stack, x, y, 0, (float) u, (float) v, width, height, 256, 256);
    }

    public static void blit(PoseStack stack, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blitWithPipeline(stack, RenderPipelines.GUI_TEXTURE, x, y, z, u, v, width, height, textureWidth, textureHeight);
    }

    public static void blitWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight
    ) {
        blitWithPipeline(stack, pipeline, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    public static void blit(PoseStack stack, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        blitWithPipeline(stack, RenderPipelines.GUI_TEXTURE, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void blitWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight
    ) {
        blitWithPipeline(stack, pipeline, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    public static void blit(PoseStack stack, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blitWithPipeline(stack, RenderPipelines.GUI_TEXTURE, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public static void blitWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight
    ) {
        blitWithPipeline(stack, pipeline, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    public static void blit(PoseStack stack, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        blitWithPipeline(stack, RenderPipelines.GUI_TEXTURE, x0, x1, y0, y1, z, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    public static void blitWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight
    ) {
        blitWithPipeline(stack, pipeline, x0, x1, y0, y1, z, (u + 0.0F) / (float) textureWidth, (u + (float) regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight, (v + (float) regionHeight) / (float) textureHeight);
    }

    public static void blit(PoseStack stack, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        blitWithPipeline(stack, RenderPipelines.GUI_TEXTURE, x0, x1, y0, y1, z, u0, u1, v0, v1);
    }

    public static void blitWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1
    ) {
        BufferBuilder buffer = RenderUtil.beginBuffer(pipeline);

        VertexBuilder.create(buffer).position(stack, (float) x0, (float) y1, (float) z).uv(u0, v1).end();
        VertexBuilder.create(buffer).position(stack, (float) x1, (float) y1, (float) z).uv(u1, v1).end();
        VertexBuilder.create(buffer).position(stack, (float) x1, (float) y0, (float) z).uv(u1, v0).end();
        VertexBuilder.create(buffer).position(stack, (float) x0, (float) y0, (float) z).uv(u0, v0).end();

        drawBuffer(buffer, pipeline);
    }

    public static void blitColorWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight,
            int red, int green, int blue, int alpha
    ) {
        blitColorWithPipeline(stack, pipeline, x, y, u, v, width, height, width, height, textureWidth, textureHeight, red, green, blue, alpha);
    }

    public static void blitColorWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int textureWidth, int textureHeight,
            int red, int green, int blue, int alpha
    ) {
        blitColorWithPipeline(stack, pipeline, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight, red, green, blue, alpha);
    }

    public static void blitColorWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight,
            int red, int green, int blue, int alpha
    ) {
        blitColorWithPipeline(
                stack,
                pipeline,
                x0, x1, y0, y1, z,
                (u + 0.0F) / (float) textureWidth,
                (u + (float) regionWidth) / (float) textureWidth,
                (v + 0.0F) / (float) textureHeight,
                (v + (float) regionHeight) / (float) textureHeight,
                red, green, blue, alpha
        );
    }

    public static void blitColorWithPipeline(
            @NotNull PoseStack stack,
            @NotNull RenderPipeline pipeline,
            int x0, int x1, int y0, int y1, int z,
            float u0, float u1, float v0, float v1,
            int red, int green, int blue, int alpha
    ) {
        BufferBuilder buffer = RenderUtil.beginBuffer(pipeline);

        VertexBuilder.create(buffer)
                .position(stack, x0, y1, z)
                .uv(u0, v1)
                .color(red, green, blue, alpha)
                .end();
        VertexBuilder.create(buffer)
                .position(stack, x1, y1, z)
                .uv(u1, v1)
                .color(red, green, blue, alpha)
                .end();
        VertexBuilder.create(buffer)
                .position(stack, x1, y0, z)
                .uv(u1, v0)
                .color(red, green, blue, alpha)
                .end();
        VertexBuilder.create(buffer)
                .position(stack, x0, y0, z)
                .uv(u0, v0)
                .color(red, green, blue, alpha)
                .end();

        drawBuffer(buffer, pipeline);
    }

    public static void blitColor(PoseStack stack,
                                 int x0, int x1, int y0, int y1, int z,
                                 float u0, float u1, float v0, float v1,
                                 int red, int green, int blue, int alpha) {
        blitColorWithPipeline(stack, RenderPipelines.GUI_TEXTURE_COLOR, x0, x1, y0, y1, z, u0, u1, v0, v1, red, green, blue, alpha);
    }

    public static void drawStringInBatch(PoseStack stack, String text, int x, int y, int color, boolean shadow) {
        drawStringInBatch(stack, text, x, y, color, shadow, false);
    }

    public static void drawStringInBatch(PoseStack stack, String text, int x, int y, int color, boolean shadow, boolean seeThrough) {
        //#if MC>=26.2
        //$$ throw new UnsupportedOperationException("drawStringInBatch is no longer supported in 26.2");
        //#else
        Font.DisplayMode displayMode = seeThrough ? Font.DisplayMode.SEE_THROUGH : TEXT_LAYER_TYPE;
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        Minecraft.getInstance().font.drawInBatch(text, x, y, color, shadow, stack.last().pose(), irendertypebuffer$impl, displayMode, 0, 15728880);
        irendertypebuffer$impl.endBatch();
        //#endif

        //#if MC<12105
        resetTextGlState();
        //#endif
    }

    public static void drawStringInBatch(PoseStack stack, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        drawStringInBatch(stack, text, x, y, color, shadow, false);
    }

    public static void drawStringInBatch(PoseStack stack, FormattedCharSequence text, int x, int y, int color, boolean shadow, boolean seeThrough) {
        //#if MC>=26.2
        //$$ throw new UnsupportedOperationException("drawStringInBatch is no longer supported in 26.2");
        //#else
        Font.DisplayMode displayMode = seeThrough ? Font.DisplayMode.SEE_THROUGH : TEXT_LAYER_TYPE;
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        Minecraft.getInstance().font.drawInBatch(text, x, y, color, shadow, stack.last().pose(), irendertypebuffer$impl, displayMode, 0, 15728880);
        irendertypebuffer$impl.endBatch();
        //#endif

        //#if MC<12105
        resetTextGlState();
        //#endif
    }

    //#if MC<12105
    private static void resetTextGlState() {
        if (CURRENT_GL_STATE != null) {
            CURRENT_GL_STATE.setDepthFunc(null);
            CURRENT_GL_STATE.setBlendFunc(null);
        }
    }
    //#endif

    public static int drawCenteredString(PoseStack stack, String string, int x, int y, int color) {
        return drawCenteredString(stack, string, x, y, color, true);
    }

    public static int drawCenteredString(PoseStack stack, String string, int x, int y, int color, boolean dropShadow) {
        color = adjustColor(color);

        int centeredX = x - getStringWidth(string) / 2;

        drawStringInBatch(
                stack,
                string,
                centeredX,
                y,
                color,
                dropShadow
        );

        return getStringX(string, centeredX, dropShadow);
    }

    public static int drawCenteredString(PoseStack stack, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        color = adjustColor(color);

        int centeredX = x - getTextWidth(text) / 2;

        drawStringInBatch(
                stack,
                text,
                centeredX,
                y,
                color,
                dropShadow
        );

        return getStringX(text, centeredX, dropShadow);
    }

    public static int drawCenteredString(PoseStack stack, McTextComponent text, int x, int y, int color) {
        return drawCenteredString(stack, getVisualOrderText(text), x, y, color, true);
    }

    public static void drawCenteredOrderedString(PoseStack stack, McTextComponent text, int width, int x, int y, int color) {
        drawCenteredOrderedString(stack, text, width, x, y, color, true);
    }

    public static void drawCenteredOrderedString(PoseStack stack, McTextComponent text, int width, int x, int y, int color, boolean dropShadow) {
        drawCenteredString(stack, getTruncatedText(text, width), x, y, color, dropShadow);
    }

    public static void drawOrderedString(PoseStack stack, McTextComponent text, int width, int x, int y, int color) {
        drawOrderedString(stack, text, width, x, y, color, true);
    }

    public static void drawOrderedString(PoseStack stack, McTextComponent text, int width, int x, int y, int color, boolean dropShadow) {
        drawString(stack, getTruncatedText(text, width), x, y, color, dropShadow, false);
    }

    public static int drawString(PoseStack stack, String string, int x, int y, int color) {
        return drawString(stack, string, x, y, color, false);
    }

    public static int drawString(PoseStack stack, String string, int x, int y, int color, boolean dropShadow) {
        return drawString(stack, string, x, y, color, dropShadow, false);
    }

    public static int drawString(PoseStack stack, String string, int x, int y, int color, boolean dropShadow, boolean seeThrough) {
        color = adjustColor(color);

        drawStringInBatch(
                stack,
                string,
                x,
                y,
                color,
                dropShadow,
                seeThrough
        );

        return getStringX(string, x, dropShadow);
    }

    public static int drawString(PoseStack stack, FormattedCharSequence text, int x, int y, int color, boolean dropShadow, boolean seeThrough) {
        color = adjustColor(color);

        drawStringInBatch(
                stack,
                text,
                x,
                y,
                color,
                dropShadow,
                seeThrough
        );

        return getStringX(text, x, dropShadow);
    }

    public static int drawString(PoseStack stack, McTextComponent text, int x, int y, int color) {
        return drawString(stack, text, x, y, color, true);
    }

    public static int drawString(PoseStack stack, McTextComponent text, int x, int y, int color, boolean dropShadow) {
        return drawString(stack, getVisualOrderText(text), x, y, color, dropShadow, false);
    }

    public static int drawStringMultiLine(PoseStack stack, McTextComponent text, int x, int y, int color, int width) {
        return drawStringMultiLine(stack, text, x, y, color, width, true);
    }

    public static int drawStringMultiLine(PoseStack stack, McTextComponent text, int x, int y, int color, int width, boolean dropShadow) {
        color = adjustColor(color);

        List<FormattedCharSequence> lines = splitLines(text, width);
        int lineHeight = getFontHeight();

        for (Iterator<FormattedCharSequence> line = lines.iterator(); line.hasNext(); y += lineHeight) {
            FormattedCharSequence orderedText = line.next();

            drawStringInBatch(
                    stack,
                    orderedText,
                    x,
                    y - lineHeight - 1,
                    color,
                    dropShadow
            );
        }

        return lines.size();
    }

    public static int drawStringMultiLineCentered(PoseStack stack, McTextComponent text, int width, int y, int yGap, int color) {
        return drawStringMultiLineCentered(stack, text, width, y, yGap, color, true);
    }

    public static int drawStringMultiLineCentered(PoseStack stack, McTextComponent text, int width, int y, int yGap, int color, boolean dropShadow) {
        color = adjustColor(color);

        List<FormattedCharSequence> lines = splitLines(text, width);
        int lineHeight = getFontHeight();

        for (Iterator<FormattedCharSequence> line = lines.iterator(); line.hasNext(); y += lineHeight + yGap) {
            FormattedCharSequence orderedText = line.next();

            drawStringInBatch(
                    stack,
                    orderedText,
                    width / 2 - getTextWidth(orderedText) / 2,
                    y + lineHeight,
                    color,
                    dropShadow
            );
        }

        return lines.size();
    }

    public static int getStringX(String string, int x, boolean dropShadow) {
        return x + getStringWidth(string) + (dropShadow ? 1 : 0);
    }

    public static int getStringX(FormattedCharSequence text, int x, boolean dropShadow) {
        return x + getTextWidth(text) + (dropShadow ? 1 : 0);
    }

    public static int adjustColor(int color) {
        return (color & -67108864) == 0 ? color | -16777216 : color;
    }

    public static String stringToWidth(String string, int width, boolean tail) {
        return stringToWidth(string, width, tail, "...");
    }

    public static String stringToWidth(String string, int width, boolean tail, String trimmedTextSuffix) {
        Font font = Minecraft.getInstance().font;
        if (font.width(string) <= width) return string;

        String trimmed = font.plainSubstrByWidth(string, width - font.width(trimmedTextSuffix), tail);
        return tail ? trimmedTextSuffix + trimmed : trimmed + trimmedTextSuffix;
    }

    public static String stringToWidth(String string, int width) {
        return stringToWidth(string, width, false);
    }

    public static int getTextWidth(McTextComponent text) {
        return Minecraft.getInstance().font.width(getTextConverter().convert(text));
    }

    public static int getTextWidth(FormattedCharSequence text) {
        return Minecraft.getInstance().font.width(text);
    }

    public static FormattedCharSequence getVisualOrderText(McTextComponent text) {
        return getTextConverter().convert(text).getVisualOrderText();
    }

    public static FormattedCharSequence getTruncatedText(McTextComponent text, int width) {
        Font font = Minecraft.getInstance().font;
        Component component = getTextConverter().convert(text);
        if (font.width(component) <= width) return component.getVisualOrderText();

        FormattedText truncated = font.substrByWidth(component, width - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(truncated, CommonComponents.ELLIPSIS));
    }

    public static List<FormattedCharSequence> splitLines(McTextComponent text, int width) {
        return Minecraft.getInstance().font.split(getTextConverter().convert(text), width);
    }

    public static int getStringWidth(String string) {
        return Minecraft.getInstance().font.width(string);
    }

    public static int getFontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public static ClientTextConverter getTextConverter() {
        return TEXT_CONVERTER;
    }
}

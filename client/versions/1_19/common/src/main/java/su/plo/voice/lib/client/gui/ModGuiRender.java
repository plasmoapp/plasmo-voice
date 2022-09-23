package su.plo.voice.lib.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.render.MinecraftTesselator;
import su.plo.lib.client.render.VertexBuilder;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.chat.TextConverter;
import su.plo.voice.lib.client.render.ModMatrix;
import su.plo.voice.lib.client.render.ModShaders;
import su.plo.voice.lib.client.render.ModVertexBuilder;
import su.plo.voice.lib.client.texture.ResourceCache;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class ModGuiRender implements GuiRender {

    private static final Minecraft minecraft = Minecraft.getInstance();

    @Getter
    private final MinecraftTesselator tesselator;
    private final TextConverter<Component> textConverter;
    private final ResourceCache resources;
    @Getter
    private final ModMatrix matrix = new ModMatrix();

    @Setter
    @Getter
    private int blitOffset;

    @Override
    public void enableScissor(int x0, int y0, int x1, int y1) {
        Window window = minecraft.getWindow();
        int i = window.getHeight();
        double d = window.getGuiScale();
        double e = (double)x0 * d;
        double f = (double)i - (double)y1 * d;
        double g = (double)(x1 - x0) * d;
        double h = (double)(y1 - y0) * d;
        RenderSystem.enableScissor((int)e, (int)f, Math.max(0, (int)g), Math.max(0, (int)h));
    }

    @Override
    public void disableScissor() {
        RenderSystem.disableScissor();
    }

    @Override
    public void hLine(int x0, int x1, int y, int color) {
        if (x1 < x0) {
            int m = x0;
            x0 = x1;
            x1 = m;
        }

        // fill -> x0 y0 x1 y0 color
        fill(x0, y, x1 + 1, y + 1, color);
    }

    @Override
    public void vLine(int x, int y0, int y1, int color) {
        if (y1 < y0) {
            int m = y0;
            y0 = y1;
            y1 = m;
        }

        fill(x, y0 + 1, x + 1, y1, color);
    }

    @Override
    public void fill(int x0, int y0, int x1, int y1, int color) {
        Matrix4f matrix4f = matrix.getPoseStack().last().pose();

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

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float o = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, (float)x0, (float)y1, 0F).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, 0F).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)x1, (float)y0, 0F).color(g, h, o, f).endVertex();
        bufferBuilder.vertex(matrix4f, (float)x0, (float)y0, 0F).color(g, h, o, f).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override
    public void fillGradient(int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z, boolean defaultBlend) {
        int f = colorStart >> 24 & 255;
        int g = colorStart >> 16 & 255;
        int h = colorStart >> 8 & 255;
        int i = colorStart & 255;
        int j = colorEnd >> 24 & 255;
        int k = colorEnd >> 16 & 255;
        int l = colorEnd >> 8 & 255;
        int m = colorEnd & 255;

        fillGradient(
                startX, startY, endX, endY,
                g, h, i, f,
                k, l, m, j,
                z,
                defaultBlend
        );
    }

    @Override
    public void fillGradient(int startX, int startY, int endX, int endY,
                             int startRed, int startBlue, int startGreen, int startAlpha,
                             int endRed, int endBlue, int endGreen, int endAlpha,
                             int z, boolean defaultBlend) {
        if (defaultBlend) {
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        fillGradient(
                matrix.getPoseStack().last().pose(), builder, startX, startY, endX, endY, z,
                startRed, startBlue,  startGreen, startAlpha,
                endRed, endBlue, endGreen, endAlpha
        );

        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private void fillGradient(Matrix4f matrix, BufferBuilder builder, int startX, int startY, int endX, int endY, int z,
                              int startRed, int startBlue, int startGreen, int startAlpha,
                              int endRed, int endBlue, int endGreen, int endAlpha) {
        builder.vertex(matrix, (float)endX, (float)startY, (float)z)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        builder.vertex(matrix, (float)startX, (float)startY, (float)z)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        builder.vertex(matrix, (float)startX, (float)endY, (float)z)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
        builder.vertex(matrix, (float)endX, (float)endY, (float)z)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
    }

    @Override
    public int drawCenteredString(String string, int x, int y, int color) {
        return minecraft.font.drawShadow(matrix.getPoseStack(), string, (float)(x - minecraft.font.width(string) / 2), (float)y, color);
    }

    @Override
    public int drawCenteredString(TextComponent text, int x, int y, int color) {
        Component component = textConverter.convert(text);

        FormattedCharSequence formattedCharSequence = component.getVisualOrderText();
        return minecraft.font.drawShadow(matrix.getPoseStack(), formattedCharSequence, (float)(x - minecraft.font.width(formattedCharSequence) / 2), (float)y, color);
    }

    @Override
    public int drawString(String string, int x, int y, int color) {
        return minecraft.font.drawShadow(matrix.getPoseStack(), string, (float) x, (float) y, color);
    }

    @Override
    public int drawString(TextComponent text, int x, int y, int color) {
        return minecraft.font.drawShadow(matrix.getPoseStack(), textConverter.convert(text), (float)x, (float)y, color);
    }

    @Override
    public int drawStringMultiLine(TextComponent text, int x, int y, int color, int width) {
        List<FormattedCharSequence> lines = minecraft.font.split(textConverter.convert(text), width);
        int lineHeight = minecraft.font.lineHeight;

        for (Iterator<FormattedCharSequence> var7 = lines.iterator(); var7.hasNext(); y += lineHeight) {
            FormattedCharSequence orderedText = var7.next();
            minecraft.font.drawShadow(matrix.getPoseStack(), orderedText, x, (float) (y - lineHeight - 1), -8355712);
        }

        return lines.size();
    }

    @Override
    public int drawCenteredOrderedString(TextComponent text, int width, int x, int y, int color) {
        FormattedCharSequence orderedText = getOrderedText(textConverter.convert(text), width);
        return minecraft.font.drawShadow(
                matrix.getPoseStack(),
                orderedText,
                x - minecraft.font.width(orderedText) / 2F,
                y,
                color
        );
    }

    @Override
    public int drawOrderedString(TextComponent text, int width, int x, int y, int color) {
        return minecraft.font.drawShadow(
                matrix.getPoseStack(),
                getOrderedText(textConverter.convert(text), width),
                x,
                y,
                color
        );
    }

    @Override
    public void blit(int x, int y, int u, int v, int width, int height) {
        blit(x, y, blitOffset, (float)u, (float)v, width, height, 256, 256);
    }

    @Override
    public void blit(int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    @Override
    public void blit(int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        blit(x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    @Override
    public void blit(int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    @Override
    public void blit(int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        blit(x0, x1, y0, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }

    @Override
    public void blit(int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        Matrix4f matrix4f = matrix.getPoseStack().last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, (float)x0, (float)y1, (float)z).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix4f, (float)x1, (float)y0, (float)z).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrix4f, (float)x0, (float)y0, (float)z).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    @Override
    public void blitColor(int x0, int x1, int y0, int y1, int z,
                          float u0, float u1, float v0, float v1,
                          int red, int green, int blue, int alpha) {
        Matrix4f matrix4f = matrix.getPoseStack().last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder
                .vertex(matrix4f, x0, y1, z)
                .uv(u0, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix4f, x1, y1, z)
                .uv(u1, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix4f, x1, y0, z)
                .uv(u1, v0)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix4f, x0, y0, z)
                .uv(u0, v0)
                .color(red, green, blue, alpha)
                .endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    @Override
    public void blitColorShader(@NotNull VertexBuilder.Shader shader, @NotNull VertexBuilder.Format format,
                                int x0, int x1, int y0, int y1, int z,
                                float u0, float u1, float v0, float v1,
                                int red, int green, int blue, int alpha) {
        Matrix4f matrix4f = matrix.getPoseStack().last().pose();

        VertexFormat vertexFormat = ModVertexBuilder.getFormat(format);
        if (vertexFormat == null) return;

        RenderSystem.setShader(getMinecraftShader(shader));
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, vertexFormat);
        bufferBuilder
                .vertex(matrix4f, (float)x0, (float)y1, (float)z)
                .uv(u0, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix4f, (float)x1, (float)y1, (float)z)
                .uv(u1, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix4f, (float)x1, (float)y0, (float)z)
                .uv(u1, v0)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferBuilder
                .vertex(matrix4f, (float)x0, (float)y0, (float)z)
                .uv(u0, v0)
                .color(red, green, blue, alpha)
                .endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    @Override
    public void setShader(@NotNull VertexBuilder.Shader shader) {
        RenderSystem.setShader(getMinecraftShader(shader));
    }

    @Override
    public void setShaderTexture(int texture, @NotNull String resourceLocation) {
        RenderSystem.setShaderTexture(0, resources.getLocation(resourceLocation));
    }

    @Override
    public void setShaderColor(float red, float green, float blue, float alpha) {
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    @Override
    public void enableBlend() {
        RenderSystem.enableBlend();
    }

    @Override
    public void disableBlend() {
        RenderSystem.disableBlend();
    }

    @Override
    public void blendFunc(int sourceFactor, int destFactor) {
        RenderSystem.blendFunc(sourceFactor, destFactor);
    }

    @Override
    public void blendFuncSeparate(int sourceFactor, int destFactor, int sourceFactor1, int destFactor1) {
        RenderSystem.blendFuncSeparate(
                sourceFactor, destFactor,
                sourceFactor1, destFactor1
        );
    }

    @Override
    public void defaultBlendFunc() {
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public void enableDepthTest() {
        RenderSystem.enableDepthTest();
    }

    @Override
    public void disableDepthTest() {
        RenderSystem.disableDepthTest();
    }

    @Override
    public void depthFunc(int func) {
        RenderSystem.depthFunc(func);
    }

    @Override
    public void depthMask(boolean mask) {
        RenderSystem.depthMask(mask);
    }

    @Override
    public void enableTexture() {
        RenderSystem.enableTexture();
    }

    @Override
    public void disableTexture() {
        RenderSystem.disableTexture();
    }

    @Override
    public void enableColorLogicOp() {
        RenderSystem.enableColorLogicOp();
    }

    @Override
    public void disableColorLogicOp() {
        RenderSystem.disableColorLogicOp();
    }

    @Override
    public void logicOp(@NotNull String logicOp) {
        RenderSystem.logicOp(GlStateManager.LogicOp.valueOf(logicOp));
    }

    @Override
    public void turnOnLightLayer() {
        minecraft.gameRenderer.lightTexture().turnOnLightLayer();
    }

    @Override
    public void turnOffLightLayer() {
        minecraft.gameRenderer.lightTexture().turnOffLightLayer();
    }

    private Supplier<ShaderInstance> getMinecraftShader(@NotNull VertexBuilder.Shader shader) {
        return switch (shader) {
            case POSITION -> GameRenderer::getPositionShader;
            case POSITION_TEX -> GameRenderer::getPositionTexShader;
            case POSITION_COLOR -> GameRenderer::getPositionColorShader;
            case POSITION_TEX_COLOR -> GameRenderer::getPositionTexColorShader;
            case POSITION_TEX_SOLID_COLOR -> ModShaders::getPositionTexSolidColorShader;
            case POSITION_COLOR_TEX_LIGHTMAP -> GameRenderer::getPositionColorTexLightmapShader;
            case RENDERTYPE_TEXT -> GameRenderer::getRendertypeTextShader;
            case RENDERTYPE_TEXT_SEE_THROUGH -> GameRenderer::getRendertypeTextSeeThroughShader;
            default -> null;
        };

    }

    private FormattedCharSequence getOrderedText(@NotNull Component text, int width) {
        Font font = minecraft.font;

        int i = font.width(text);
        if (i > width) {
            FormattedText stringVisitable = FormattedText.composite(font.substrByWidth(text, width - font.width("...")), FormattedText.of("..."));
            return Language.getInstance().getVisualOrder(stringVisitable);
        } else {
            return text.getVisualOrderText();
        }
    }
}

package su.plo.lib.client.gui;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.render.MinecraftMatrix;
import su.plo.lib.client.render.MinecraftTesselator;
import su.plo.lib.client.render.VertexBuilder;

public interface GuiRender {

    void enableScissor(int x0, int y0, int x1, int y1);

    void disableScissor();

    void hLine(int x0, int x1, int y0, int color);

    void vLine(int x, int y0, int y1, int color);

    // colors
    void fill(int x0, int y0, int x1, int y1, int color);

    void fillGradient(int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z, boolean defaultBlend);

    void fillGradient(int startX, int startY, int endX, int endY,
                      int startRed, int startBlue, int startGreen, int startAlpha,
                      int endRed, int endBlue, int endGreen, int endAlpha,
                      int z,
                      boolean defaultBlend);

    // text
    int drawCenteredString(String string, int x, int y, int color);

    int drawCenteredString(TextComponent component, int x, int y, int color);

    int drawString(String string, int x, int y, int color);

    int drawString(TextComponent text, int x, int y, int color);

    int drawString(TextComponent text, int x, int y, int color, boolean dropShadow);

    int drawString(TextComponent component, float x, float y, int color, boolean dropShadow, boolean seeThrough, int backgroundColor, int light);

    int drawStringMultiLine(TextComponent text, int x, int y, int color, int width);

    int drawCenteredOrderedString(TextComponent text, int width, int x, int y, int color);

    int drawOrderedString(TextComponent text, int width, int x, int y, int color);

    // textures
    void blit(int x, int y, int u, int v, int width, int height);

    void blit(int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight);

    void blit(int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight);

    void blit(int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight);

    void blit(int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight);

    void blit(int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1);

    void blitColor(int x0, int x1, int y0, int y1, int z,
                   float u0, float u1, float v0, float v1,
                   int red, int green, int blue, int alpha);

    void blitColorShader(@NotNull VertexBuilder.Shader shader,
                         @NotNull VertexBuilder.Format format,
                         int x0, int x1, int y0, int y1, int z,
                         float u0, float u1, float v0, float v1,
                         int red, int green, int blue, int alpha);

    int getBlitOffset();

    void setBlitOffset(int blitOffset);

    // RenderSystem
    void setShader(@NotNull VertexBuilder.Shader shader);

    void setShaderTexture(int texture, @NotNull String resourceLocation);

    void setShaderColor(float red, float green, float blue, float alpha);

    void enableBlend();

    void disableBlend();

    void blendFunc(int sourceFactor, int destFactor);

    void blendFuncSeparate(int sourceFactor, int destFactor,
                           int sourceFactor1, int destFactor1);

    void defaultBlendFunc();

    void enableDepthTest();

    void disableDepthTest();

    void depthFunc(int func);

    void depthMask(boolean mask);

    void enableTexture();

    void disableTexture();

    void enableColorLogicOp();

    void disableColorLogicOp();

    void logicOp(@NotNull String logicOp);

    void turnOnLightLayer();

    void turnOffLightLayer();

    void enableCull();

    void disableCull();

    void enablePolygonOffset();

    void disablePolygonOffset();

    void polygonOffset(float o1, float o2);

    void lineWidth(float width);

    @NotNull MinecraftTesselator getTesselator();

    @NotNull MinecraftMatrix getMatrix();
}

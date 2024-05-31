package su.plo.lib.mod.client.render;

//#if MC>=11904
import net.minecraft.client.gui.Font;
//#endif

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import gg.essential.universal.TextBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture;
import su.plo.slib.api.chat.component.McTextComponent;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.MultiBufferSource;
import su.plo.lib.mod.client.chat.ClientTextConverter;

import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

//#if MC>=11700
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.renderer.ShaderInstance;
//#endif

@UtilityClass
public class RenderUtil {

    //#if MC>=11904
    private static final Font.DisplayMode TEXT_LAYER_TYPE = Font.DisplayMode.NORMAL;
    //#else
    //$$ private static final boolean TEXT_LAYER_TYPE = false;
    //#endif

    private static final ClientTextConverter TEXT_CONVERTER = new ClientTextConverter();

    public static void enableScissor(int x, int y, int width, int height) {
        double scaleFactor = Minecraft.getInstance().getWindow().getGuiScale();

        double scaledX = x * scaleFactor;
        double scaledY = y * scaleFactor;
        double scaledWidth = width * scaleFactor;
        double scaledHeight = height * scaleFactor;

        //#if MC<11502
        //$$ GL11.glEnable(GL11.GL_SCISSOR_TEST);
        //$$ GL11.glScissor((int) scaledX, (int) scaledY, Math.max(0, (int) scaledWidth), Math.max(0, (int) scaledHeight));
        //#else
        RenderSystem.enableScissor((int) scaledX, (int) scaledY, Math.max(0, (int) scaledWidth), Math.max(0, (int) scaledHeight));
        //#endif
    }

    public static void disableScissor() {
        //#if MC<11502
        //$$ GL11.glDisable(GL11.GL_SCISSOR_TEST);
        //#else
        RenderSystem.disableScissor();
        //#endif
    }

    //#if MC>=11700
    // Note: Needs to be an Identity hash map because VertexFormat's equals method is broken (compares via its
    //       component Map but order very much matters for VertexFormat) as of 1.17
    private static final Map<VertexFormat, Supplier<ShaderInstance>> DEFAULT_SHADERS = new IdentityHashMap<>();
    static {
        DEFAULT_SHADERS.put(DefaultVertexFormat.PARTICLE, GameRenderer::getParticleShader);
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION, GameRenderer::getPositionShader);
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_COLOR, GameRenderer::getPositionColorShader);
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, GameRenderer::getPositionColorLightmapShader);
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_TEX, GameRenderer::getPositionTexShader);
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_COLOR_TEX, GameRenderer::getPositionColorTexShader);
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_TEX_COLOR, GameRenderer::getPositionTexColorShader);
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, GameRenderer::getPositionColorTexLightmapShader);
    //#if MC>=12005
    //$$     // Shaders for these formats are no longer provided.
    //#else
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, GameRenderer::getPositionTexLightmapColorShader);
        DEFAULT_SHADERS.put(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, GameRenderer::getPositionTexColorNormalShader);
    //#endif
    }
    //#endif

    public static void beginBufferWithDefaultShader(@NotNull BufferBuilder buffer, @NotNull VertexFormatMode mode, @NotNull VertexFormat format) {
        //#if MC>=11700
        Supplier<ShaderInstance> supplier = DEFAULT_SHADERS.get(format);
        if (supplier == null) {
            throw new IllegalArgumentException("No default shader for " + format + ". Bind your own and use beginBufferWithActiveShader instead.");
        }

        RenderSystem.setShader(supplier);
        //#endif

        beginBufferWithActiveShader(buffer, mode, format);
    }

    public static void beginBufferWithActiveShader(@NotNull BufferBuilder buffer, @NotNull VertexFormatMode mode, @NotNull VertexFormat format) {
        //#if MC>=11700
        buffer.begin(mode.toMc(), format);
        //#else
        //$$ buffer.begin(mode.getGlMode(), format);
        //#endif
    }

    public static void bindTexture(int index, @NotNull ResourceLocation location) {
        //#if MC>=11700
        RenderSystem.setShaderTexture(index, location);
        //#else
        int glTextureId = getOrLoadTextureId(location);
        configureTextureUnit(index, () -> RenderSystem.bindTexture(glTextureId));
        //#endif
    }

    public static void configureTextureUnit(int index, Runnable block) {
        int prevActiveTexture = getActiveTexture();
        setActiveTexture(GL_TEXTURE0 + index);

        block.run();

        setActiveTexture(prevActiveTexture);
    }

    public static int getActiveTexture() {
        return GL11.glGetInteger(GL_ACTIVE_TEXTURE);
    }

    public static void setActiveTexture(int glId) {
        GlStateManager._activeTexture(glId);
    }

    public static int getOrLoadTextureId(ResourceLocation resourceLocation) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture texture = textureManager.getTexture(resourceLocation);
        if (texture == null) {
            texture = new SimpleTexture(resourceLocation);
            textureManager.register(resourceLocation, (AbstractTexture)texture);
        }

        return ((AbstractTexture)texture).getId();
    }

    public static void fill(PoseStack stack, int x0, int y0, int x1, int y1, int color) {
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

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.enableBlend();
        defaultBlendFunc();

        RenderUtil.beginBufferWithDefaultShader(buffer, VertexFormatMode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(stack.last().pose(), (float) x0, (float) y1, 0F).color(g, h, o, f).endVertex();
        buffer.vertex(stack.last().pose(), (float) x1, (float) y1, 0F).color(g, h, o, f).endVertex();
        buffer.vertex(stack.last().pose(), (float) x1, (float) y0, 0F).color(g, h, o, f).endVertex();
        buffer.vertex(stack.last().pose(), (float) x0, (float) y0, 0F).color(g, h, o, f).endVertex();
        tesselator.end();

        RenderSystem.disableBlend();
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
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderUtil.beginBufferWithDefaultShader(buffer, VertexFormatMode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        fillGradient(
                stack, buffer, startX, startY, endX, endY, z,
                startRed, startBlue, startGreen, startAlpha,
                endRed, endBlue, endGreen, endAlpha
        );

        tesselator.end();
    }

    private static void fillGradient(PoseStack stack, BufferBuilder buffer,
                                     int startX, int startY, int endX, int endY, int z,
                                     int startRed, int startBlue, int startGreen, int startAlpha,
                                     int endRed, int endBlue, int endGreen, int endAlpha) {
        buffer.vertex(stack.last().pose(), (float) endX, (float) startY, (float) z)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), (float) startX, (float) startY, (float) z)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), (float) startX, (float) endY, (float) z)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), (float) endX, (float) endY, (float) z)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
    }

    public static void blitSprite(
            @NotNull PoseStack stack,
            @NotNull GuiWidgetTexture sprite,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        blit(stack, x, y, u + sprite.getU(), v + sprite.getV(), width, height, sprite.getTextureWidth(), sprite.getTextureHeight());
    }

    public static void blit(PoseStack stack, int x, int y, int u, int v, int width, int height) {
        blit(stack, x, y, 0, (float) u, (float) v, width, height, 256, 256);
    }

    public static void blit(PoseStack stack, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(stack, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    public static void blit(PoseStack stack, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        blit(stack, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    public static void blit(PoseStack stack, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(stack, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    public static void blit(PoseStack stack, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        blit(stack, x0, x1, y0, y1, z, (u + 0.0F) / (float) textureWidth, (u + (float) regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight, (v + (float) regionHeight) / (float) textureHeight);
    }

    public static void blit(PoseStack stack, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderUtil.beginBufferWithDefaultShader(buffer, VertexFormatMode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(stack.last().pose(), (float) x0, (float) y1, (float) z).uv(u0, v1).endVertex();
        buffer.vertex(stack.last().pose(), (float) x1, (float) y1, (float) z).uv(u1, v1).endVertex();
        buffer.vertex(stack.last().pose(), (float) x1, (float) y0, (float) z).uv(u1, v0).endVertex();
        buffer.vertex(stack.last().pose(), (float) x0, (float) y0, (float) z).uv(u0, v0).endVertex();

        tesselator.end();
    }

    public static void blitWithActiveShader(PoseStack stack,
                                            VertexFormat format,
                                            int x0, int x1, int y0, int y1, int z,
                                            float u0, float u1, float v0, float v1,
                                 int red, int green, int blue, int alpha) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderUtil.beginBufferWithActiveShader(buffer, VertexFormatMode.QUADS, format);

        buffer.vertex(stack.last().pose(), x0, y1, z)
                .uv(u0, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), x1, y1, z)
                .uv(u1, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), x1, y0, z)
                .uv(u1, v0)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), x0, y0, z)
                .uv(u0, v0)
                .color(red, green, blue, alpha)
                .endVertex();

        tesselator.end();
    }

    public static void blitColor(PoseStack stack,
                                 int x0, int x1, int y0, int y1, int z,
                                 float u0, float u1, float v0, float v1,
                                 int red, int green, int blue, int alpha) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderUtil.beginBufferWithDefaultShader(buffer, VertexFormatMode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        buffer.vertex(stack.last().pose(), x0, y1, z)
                .uv(u0, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), x1, y1, z)
                .uv(u1, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), x1, y0, z)
                .uv(u1, v0)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.vertex(stack.last().pose(), x0, y0, z)
                .uv(u0, v0)
                .color(red, green, blue, alpha)
                .endVertex();

        tesselator.end();
    }

    public static void drawStringInBatch(PoseStack stack, String text, int x, int y, int color, boolean shadow) {
        MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        Minecraft.getInstance().font.drawInBatch(text, x, y, color, shadow, stack.last().pose(), irendertypebuffer$impl, TEXT_LAYER_TYPE, 0, 15728880);
        irendertypebuffer$impl.endBatch();
    }

    public static int drawCenteredString(PoseStack stack, String string, int x, int y, int color) {
        color = adjustColor(color);

        int centeredX = x - getStringWidth(string) / 2;

        drawStringInBatch(
                stack,
                string,
                centeredX,
                y,
                color,
                true
        );

        return getStringX(string, centeredX, true);
    }

    public static int drawCenteredString(PoseStack stack, McTextComponent text, int x, int y, int color) {
        return drawCenteredString(stack, getFormattedString(text), x, y, color);
    }

    public static void drawCenteredOrderedString(PoseStack stack, McTextComponent text, int width, int x, int y, int color) {
        color = adjustColor(color);

        String orderedText = getOrderedString(text, width);

        drawStringInBatch(
                stack,
                orderedText,
                x - getStringWidth(orderedText) / 2,
                y,
                color,
                true
        );
    }

    public static void drawOrderedString(PoseStack stack, McTextComponent text, int width, int x, int y, int color) {
        color = adjustColor(color);

        drawStringInBatch(
                stack,
                getOrderedString(text, width),
                x,
                y,
                color,
                true
        );
    }

    public static int drawString(PoseStack stack, String string, int x, int y, int color) {
        color = adjustColor(color);

        drawStringInBatch(
                stack,
                string,
                x,
                y,
                color,
                false
        );

        return getStringX(string, x, false);
    }

    public static int drawString(PoseStack stack, McTextComponent text, int x, int y, int color) {
        return drawString(stack, text, x, y, color, true);
    }

    public static int drawString(PoseStack stack, McTextComponent text, int x, int y, int color, boolean dropShadow) {
        color = adjustColor(color);

        String formattedText = getFormattedString(text);

        drawStringInBatch(
                stack,
                formattedText,
                x,
                y,
                color,
                dropShadow
        );

        return getStringX(formattedText, x, dropShadow);
    }


    public static int drawStringLight(PoseStack stack, McTextComponent text, int x, int y, int color, int light,
                                      boolean seeThrough,
                                      boolean dropShadow) {
        color = adjustColor(color);

        String formattedText = getFormattedString(text);

        //#if MC>=11904
        Font.DisplayMode displayMode = seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
        //#else
        //$$ boolean displayMode = seeThrough;
        //#endif

        MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        Minecraft.getInstance().font.drawInBatch(
                formattedText,
                (float) x,
                (float) y,
                color,
                dropShadow,
                stack.last().pose(),
                irendertypebuffer$impl,
                displayMode,
                0,
                light
        );
        irendertypebuffer$impl.endBatch();

        return getStringX(formattedText, x, dropShadow);
    }

    public static int drawStringMultiLine(PoseStack stack, McTextComponent text, int x, int y, int color, int width) {
        color = adjustColor(color);

        String string = getFormattedString(text);

        List<String> lines = su.plo.voice.client.extension.TextKt.getStringSplitToWidth(string, width, true, true);
        int lineHeight = getFontHeight();

        for (Iterator<String> line = lines.iterator(); line.hasNext(); y += lineHeight) {
            String orderedText = line.next();

            drawStringInBatch(
                    stack,
                    orderedText,
                    x,
                    y - lineHeight - 1,
                    color,
                    true
            );
        }

        return lines.size();
    }

    public static int drawStringMultiLineCentered(PoseStack stack, McTextComponent text, int width, int y, int yGap, int color) {
        color = adjustColor(color);

        String string = getFormattedString(text);

        List<String> lines = su.plo.voice.client.extension.TextKt.getStringSplitToWidth(string, width, true, true);
        int lineHeight = getFontHeight();

        for (Iterator<String> line = lines.iterator(); line.hasNext(); y += lineHeight + yGap) {
            String orderedText = line.next();

            drawStringInBatch(
                    stack,
                    orderedText,
                    width / 2 - getStringWidth(orderedText) / 2,
                    y + lineHeight,
                    color,
                    true
            );
        }

        return lines.size();
    }

    public static int getStringX(String string, int x, boolean dropShadow) {
        return x + getStringWidth(string) + (dropShadow ? 1 : 0);
    }

    public static int adjustColor(int color) {
        return (color & -67108864) == 0 ? color | -16777216 : color;
    }

    public static String stringToWidth(String string, int width, boolean tail) {
        List<String> lines = su.plo.voice.client.extension.TextKt.splitStringToWidthTruncated(
                string,
                width,
                1,
                false,
                true,
                "..."
        );

        return lines.get(tail ? lines.size() - 1 : 0);
    }

    public static String stringToWidth(String string, int width) {
        return stringToWidth(string, width, false);
    }

    public static int getTextWidth(McTextComponent text) {
        return getStringWidth(getFormattedString(text));
    }

    public static String getOrderedString(McTextComponent text, int width) {
        return su.plo.voice.client.extension.TextKt.getTruncatedString(getFormattedString(text), width, "...");
    }

    public static String getFormattedString(McTextComponent text) {
        Component component = getTextConverter().convert(text);

        TextBuilder textBuilder = new TextBuilder(true);
        component.getVisualOrderText().accept(textBuilder);

        return textBuilder.getString();
    }

    public static void enableColorLogic() {
        //#if MC<11502
        //$$ GlStateManager.enableColorLogic();
        //#else
        RenderSystem.enableColorLogicOp();
        //#endif
    }

    public static void disableColorLogic() {
        //#if MC<11502
        //$$ GlStateManager.disableColorLogic();
        //#else
        RenderSystem.disableColorLogicOp();
        //#endif
    }

    public static void logicOp(String opcode) {
        RenderSystem.logicOp(GlStateManager.LogicOp.valueOf(opcode));
    }

    public static void enableCull() {
        //#if MC<11502
        //$$ GlStateManager.enableCull();
        //#else
        RenderSystem.enableCull();
        //#endif
    }

    public static void disableCull() {
        //#if MC<11502
        //$$ GlStateManager.disableCull();
        //#else
        RenderSystem.disableCull();
        //#endif
    }

    public static void enablePolygonOffset() {
        //#if MC<11502
        //$$ GlStateManager.enablePolygonOffset();
        //#else
        RenderSystem.enablePolygonOffset();
        //#endif
    }

    public static void disablePolygonOffset() {
        //#if MC<11502
        //$$ GlStateManager.disablePolygonOffset();
        //#else
        RenderSystem.disablePolygonOffset();
        //#endif
    }

    public static void polygonOffset(float factor, float units) {
        //#if MC<11502
        //$$ GlStateManager.doPolygonOffset(factor, units);
        //#else
        RenderSystem.polygonOffset(factor, units);
        //#endif
    }

    public static void lineWidth(float width) {
        //#if MC<11502
        //$$ GlStateManager.glLineWidth(width);
        //#else
        RenderSystem.lineWidth(width);
        //#endif
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

    public static void defaultBlendFunc() {
        RenderSystem.blendFuncSeparate(
                770,
                771,
                1,
                0
        );
    }
}

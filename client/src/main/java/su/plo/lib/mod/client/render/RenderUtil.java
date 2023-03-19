package su.plo.lib.mod.client.render;

//#if MC<11502
//$$ import net.minecraft.client.renderer.GlStateManager;
//#else

//#if MC>=11904
//$$ import net.minecraft.client.gui.Font;
//#endif

import com.mojang.blaze3d.platform.GlStateManager;
//#endif

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UMinecraft;
import gg.essential.universal.UResolution;
import gg.essential.universal.wrappers.message.UTextComponent;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.MultiBufferSource;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.chat.ClientTextConverter;

import java.util.Iterator;
import java.util.List;

import static su.plo.voice.client.utils.TextKt.*;

@UtilityClass
public class RenderUtil {

    private static final ClientTextConverter TEXT_CONVERTER = new ClientTextConverter();

    public static void enableScissor(int x, int y, int width, int height) {
        double scaleFactor = UResolution.getScaleFactor();

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

    public static void fill(UMatrixStack stack, int x0, int y0, int x1, int y1, int color) {
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
        UGraphics buffer = UGraphics.getFromTessellator();
        UGraphics.enableBlend();
        defaultBlendFunc();

        buffer.beginWithDefaultShader(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR);
        buffer.pos(stack, (float) x0, (float) y1, 0F).color(g, h, o, f).endVertex();
        buffer.pos(stack, (float) x1, (float) y1, 0F).color(g, h, o, f).endVertex();
        buffer.pos(stack, (float) x1, (float) y0, 0F).color(g, h, o, f).endVertex();
        buffer.pos(stack, (float) x0, (float) y0, 0F).color(g, h, o, f).endVertex();
        buffer.drawDirect();

        UGraphics.disableBlend();
    }

    public static void fillGradient(UMatrixStack stack,
                                    int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z, boolean defaultBlend) {
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
                z,
                defaultBlend
        );
    }

    public static void fillGradient(UMatrixStack stack,
                                    int startX, int startY, int endX, int endY,
                                    int startRed, int startBlue, int startGreen, int startAlpha,
                                    int endRed, int endBlue, int endGreen, int endAlpha,
                                    int z, boolean defaultBlend) {
        if (defaultBlend) {
//            disableTexture();
            UGraphics.enableBlend();
            defaultBlendFunc();
        }
        UGraphics buffer = UGraphics.getFromTessellator();
        buffer.beginWithDefaultShader(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR);

        fillGradient(
                stack, buffer, startX, startY, endX, endY, z,
                startRed, startBlue, startGreen, startAlpha,
                endRed, endBlue, endGreen, endAlpha
        );

        buffer.drawDirect();
        UGraphics.disableBlend();
//        enableTexture();
    }

    private static void fillGradient(UMatrixStack stack, UGraphics buffer,
                                     int startX, int startY, int endX, int endY, int z,
                                     int startRed, int startBlue, int startGreen, int startAlpha,
                                     int endRed, int endBlue, int endGreen, int endAlpha) {
        buffer.pos(stack, (float) endX, (float) startY, (float) z)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        buffer.pos(stack, (float) startX, (float) startY, (float) z)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        buffer.pos(stack, (float) startX, (float) endY, (float) z)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
        buffer.pos(stack, (float) endX, (float) endY, (float) z)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
    }

    public static void blit(UMatrixStack stack, int x, int y, int u, int v, int width, int height) {
        blit(stack, x, y, 0, (float) u, (float) v, width, height, 256, 256);
    }

    public static void blit(UMatrixStack stack, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(stack, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    public static void blit(UMatrixStack stack, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        blit(stack, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    public static void blit(UMatrixStack stack, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(stack, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    public static void blit(UMatrixStack stack, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        blit(stack, x0, x1, y0, y1, z, (u + 0.0F) / (float) textureWidth, (u + (float) regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight, (v + (float) regionHeight) / (float) textureHeight);
    }

    public static void blit(UMatrixStack stack, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
        UGraphics buffer = UGraphics.getFromTessellator();

        buffer.beginWithDefaultShader(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_TEXTURE);
        buffer.pos(stack, (float) x0, (float) y1, (float) z).tex(u0, v1).endVertex();
        buffer.pos(stack, (float) x1, (float) y1, (float) z).tex(u1, v1).endVertex();
        buffer.pos(stack, (float) x1, (float) y0, (float) z).tex(u1, v0).endVertex();
        buffer.pos(stack, (float) x0, (float) y0, (float) z).tex(u0, v0).endVertex();

        buffer.drawDirect();
    }

    public static void blitWithActiveShader(UMatrixStack stack,
                                            UGraphics.CommonVertexFormats format,
                                            int x0, int x1, int y0, int y1, int z,
                                            float u0, float u1, float v0, float v1,
                                 int red, int green, int blue, int alpha) {
        UGraphics buffer = UGraphics.getFromTessellator();
        buffer.beginWithActiveShader(UGraphics.DrawMode.QUADS, format);

        buffer.pos(stack, x0, y1, z)
                .tex(u0, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(stack, x1, y1, z)
                .tex(u1, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(stack, x1, y0, z)
                .tex(u1, v0)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(stack, x0, y0, z)
                .tex(u0, v0)
                .color(red, green, blue, alpha)
                .endVertex();

        buffer.drawDirect();
    }

    public static void blitColor(UMatrixStack stack,
                                 int x0, int x1, int y0, int y1, int z,
                                 float u0, float u1, float v0, float v1,
                                 int red, int green, int blue, int alpha) {
        UGraphics buffer = UGraphics.getFromTessellator();
        buffer.beginWithDefaultShader(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_TEXTURE_COLOR);

        buffer.pos(stack, x0, y1, z)
                .tex(u0, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(stack, x1, y1, z)
                .tex(u1, v1)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(stack, x1, y0, z)
                .tex(u1, v0)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.pos(stack, x0, y0, z)
                .tex(u0, v0)
                .color(red, green, blue, alpha)
                .endVertex();

        buffer.drawDirect();
    }

    public static int drawCenteredString(UMatrixStack stack, String string, int x, int y, int color) {
        color = adjustColor(color);

        float centeredX = (float) (x - UGraphics.getStringWidth(string) / 2);

        UGraphics.drawString(
                stack,
                string,
                centeredX, (float) y,
                color,
                true
        );

        return getStringX(string, (int) centeredX, true);
    }

    public static int drawCenteredString(UMatrixStack stack, MinecraftTextComponent text, int x, int y, int color) {
        UTextComponent component = getTextConverter().convertToUniversal(text);
        return drawCenteredString(stack, component.getFormattedText(), x, y, color);
    }

    public static void drawCenteredOrderedString(UMatrixStack stack, MinecraftTextComponent text, int width, int x, int y, int color) {
        color = adjustColor(color);

        String orderedText = getOrderedString(text, width);
        UGraphics.drawString(
                stack,
                orderedText,
                x - UGraphics.getStringWidth(orderedText) / 2F,
                y,
                color,
                true
        );
    }

    public static void drawOrderedString(UMatrixStack stack, MinecraftTextComponent text, int width, int x, int y, int color) {
        color = adjustColor(color);

        UGraphics.drawString(
                stack,
                getOrderedString(text, width),
                x,
                y,
                color,
                true
        );
    }

    public static int drawString(UMatrixStack stack, String string, int x, int y, int color) {
        color = adjustColor(color);

        UGraphics.drawString(stack, string, (float) x, (float) y, color, false);
        return getStringX(string, x, false);
    }

    public static int drawString(UMatrixStack stack, MinecraftTextComponent text, int x, int y, int color) {
        return drawString(stack, text, x, y, color, true);
    }

    public static int drawString(UMatrixStack stack, MinecraftTextComponent text, int x, int y, int color, boolean dropShadow) {
        color = adjustColor(color);

        UTextComponent component = getTextConverter().convertToUniversal(text);
        String formattedText = component.getFormattedText();

        UGraphics.drawString(stack, formattedText, (float) x, (float) y, color, dropShadow);
        return getStringX(formattedText, x, dropShadow);
    }


    public static int drawStringLight(UMatrixStack stack, MinecraftTextComponent text, int x, int y, int color, int light,
                                      boolean seeThrough, // only used in 1.16.2+
                                      boolean dropShadow) {
        color = adjustColor(color);

        UTextComponent component = getTextConverter().convertToUniversal(text);
        String formattedText = component.getFormattedText();

        //#if MC<11602
        //$$ UGraphics.drawString(stack, formattedText, (float) x, (float) y, color, dropShadow);
        //#else

        //#if MC>=11904
        //$$ Font.DisplayMode displayMode = seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
        //#else
        boolean displayMode = seeThrough;
        //#endif

        MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        UMinecraft.getFontRenderer().drawInBatch(
                formattedText,
                (float) x,
                (float) y,
                color,
                dropShadow,
                stack.peek().getModel(),
                irendertypebuffer$impl,
                displayMode,
                0,
                light
        );
        irendertypebuffer$impl.endBatch();
        //#endif

        return getStringX(formattedText, x, dropShadow);
    }

    public static int drawStringMultiLine(UMatrixStack stack, MinecraftTextComponent text, int x, int y, int color, int width) {
        color = adjustColor(color);

        String string = getFormattedString(text);

        List<String> lines = getStringSplitToWidth(string, width, true, true);
        int lineHeight = UGraphics.getFontHeight();

        for (Iterator<String> line = lines.iterator(); line.hasNext(); y += lineHeight) {
            String orderedText = line.next();
            UGraphics.drawString(stack, orderedText, x, (float) (y - lineHeight - 1), color, true);
        }

        return lines.size();
    }

    public static int drawStringMultiLineCentered(UMatrixStack stack, MinecraftTextComponent text, int width, int y, int yGap, int color) {
        color = adjustColor(color);

        String string = getFormattedString(text);

        List<String> lines = getStringSplitToWidth(string, width, true, true);
        int lineHeight = UGraphics.getFontHeight();

        for (Iterator<String> line = lines.iterator(); line.hasNext(); y += lineHeight + yGap) {
            String orderedText = line.next();
            UGraphics.drawString(
                    stack,
                    orderedText,
                    (float) width / 2 - (float) UGraphics.getStringWidth(orderedText) / 2,
                    (float) y + lineHeight,
                    color,
                    true
            );
        }

        return lines.size();
    }

    public static int getStringX(String string, int x, boolean dropShadow) {
        return x + UGraphics.getStringWidth(string) + (dropShadow ? 1 : 0);
    }

    public static int adjustColor(int color) {
        return (color & -67108864) == 0 ? color | -16777216 : color;
    }

    public static String stringToWidth(String string, int width, boolean tail) {
        List<String> lines = splitStringToWidthTruncated(
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

    public static int getTextWidth(MinecraftTextComponent text) {
        UTextComponent component = getTextConverter().convertToUniversal(text);
        return UGraphics.getStringWidth(component.getFormattedText());
    }

    public static String getOrderedString(MinecraftTextComponent text, int width) {
        return getTruncatedString(getFormattedString(text), width, "...");
    }

    public static String getFormattedString(MinecraftTextComponent text) {
        return getTextConverter().convertToUniversal(text).getFormattedText();
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
        // todo: legacy
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

    public static boolean hasLightLayer() {
        return RenderSystem.getShaderTexture(2) != 0;
    }

    public static void turnOnLightLayer() {
        // todo: (legacy support) since when?
        UMinecraft.getMinecraft().gameRenderer.lightTexture().turnOnLightLayer();
    }

    public static void turnOffLightLayer() {
        UMinecraft.getMinecraft().gameRenderer.lightTexture().turnOffLightLayer();
    }


    public static ClientTextConverter getTextConverter() {
        return TEXT_CONVERTER;
    }

    public static void defaultBlendFunc() {
        UGraphics.tryBlendFuncSeparate(
                770,
                771,
                1,
                0
        );
    }
}

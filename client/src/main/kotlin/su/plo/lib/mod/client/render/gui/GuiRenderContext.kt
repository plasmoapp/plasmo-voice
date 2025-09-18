package su.plo.lib.mod.client.render.gui

import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.ScissorState
import su.plo.lib.mod.client.render.pipeline.RenderPipeline
import su.plo.lib.mod.client.render.pipeline.RenderPipelines
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.client.extension.getStringSplitToWidth

import java.awt.Color

//#if MC>=12109
//$$ import com.mojang.blaze3d.platform.cursor.CursorType
//#endif

//#if MC>=12106
//$$ import org.joml.Matrix3x2fStack
//$$ import net.minecraft.client.Minecraft
//$$ import net.minecraft.util.ARGB
//$$ import su.plo.voice.client.mixin.accessor.GuiGraphicsAccessor
//#else
import com.mojang.blaze3d.vertex.PoseStack
//#endif

//#if MC>=12000
//$$ import net.minecraft.client.gui.GuiGraphics;
//#endif

class GuiRenderContext(
    //#if MC>=12000
    //$$ val mcContext: GuiGraphics,
    //#else
    val stack: PoseStack,
    //#endif
) {

    //#if MC>=12106
    //$$ val stack: Matrix3x2fStack
    //$$     get() = mcContext.pose()
    //#elseif MC>=12000
    //$$ val stack: PoseStack
    //$$     get() = mcContext.pose()
    //#endif

    private val scissorStack: ArrayDeque<ScissorState> = ArrayDeque()

    fun applyScissorState(state: ScissorState?) {
        if (state != null) {
            scissorStack.add(state)
        } else {
            scissorStack.removeLast()
        }

        //#if MC>=12106
        //$$ if (state != null) {
        // I don't understand why, but mojang subtracts x and y from width and height respectively
        //$$     mcContext.enableScissor(state.x, state.y, state.width + state.x, state.height + state.y)
        //$$ } else {
        //$$     mcContext.disableScissor()
        //$$ }
        //#else
        ScissorState.applyScissorState(state)
        //#endif
    }

    fun getScissorState(): ScissorState? =
        scissorStack.lastOrNull()

    fun flush() {
        //#if MC>=12106
        //$$ mcContext.nextStratum();
        //#elseif MC>=12102
        //$$ mcContext.flush();
        //#endif
    }

    @JvmOverloads
    fun blit(
        textureLocation: ResourceLocation,
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        width: Int,
        height: Int,
        textureWidth: Int,
        textureHeight: Int,
        renderPipeline: RenderPipeline = RenderPipelines.GUI_TEXTURE,
    ) {
        //#if MC>=12106
        //$$ mcContext.blit(
        //$$     renderPipeline.mcRenderPipeline,
        //$$     textureLocation,
        //$$     x,
        //$$     y,
        //$$     u,
        //$$     v,
        //$$     width,
        //$$     height,
        //$$     textureWidth,
        //$$     textureHeight
        //$$ )
        //#else
        RenderUtil.bindTexture(0, textureLocation)
        RenderUtil.blitWithPipeline(
            stack,
            renderPipeline,
            x,
            y,
            u,
            v,
            width,
            height,
            textureWidth,
            textureHeight
        )
        //#endif
    }

    @JvmOverloads
    fun blit(
        textureLocation: ResourceLocation,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        u: Float,
        v: Float,
        regionWidth: Int,
        regionHeight: Int,
        textureWidth: Int,
        textureHeight: Int,
        renderPipeline: RenderPipeline = RenderPipelines.GUI_TEXTURE,
    ) {
        //#if MC>=12106
        //$$ mcContext.blit(
        //$$     renderPipeline.mcRenderPipeline,
        //$$     textureLocation,
        //$$     x,
        //$$     y,
        //$$     u,
        //$$     v,
        //$$     width,
        //$$     height,
        //$$     regionWidth,
        //$$     regionHeight,
        //$$     textureWidth,
        //$$     textureHeight
        //$$ )
        //#else
        RenderUtil.bindTexture(0, textureLocation)
        RenderUtil.blitWithPipeline(
            stack,
            renderPipeline,
            x,
            y,
            width,
            height,
            u,
            v,
            regionWidth,
            regionHeight,
            textureWidth,
            textureHeight
        )
        //#endif
    }

    @JvmOverloads
    fun blitColor(
        textureLocation: ResourceLocation,
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        width: Int,
        height: Int,
        textureWidth: Int,
        textureHeight: Int,
        color: Color,
        renderPipeline: RenderPipeline = RenderPipelines.GUI_TEXTURE_COLOR,
    ) {
        //#if MC>=12106
        //$$ mcContext.blit(
        //$$     renderPipeline.mcRenderPipeline,
        //$$     textureLocation,
        //$$     x,
        //$$     y,
        //$$     u,
        //$$     v,
        //$$     width,
        //$$     height,
        //$$     textureWidth,
        //$$     textureHeight,
        //$$     ARGB.color(color.alpha, color.red, color.green, color.blue),
        //$$ )
        //#else
        RenderUtil.bindTexture(0, textureLocation)
        RenderUtil.blitColorWithPipeline(
            stack,
            renderPipeline,
            x, y, u, v, width, height, textureWidth, textureHeight, color.red, color.green, color.blue, color.alpha
        )
        //#endif
    }

    @JvmOverloads
    fun blitColor(
        textureLocation: ResourceLocation,
        x0: Int,
        x1: Int,
        y0: Int,
        y1: Int,
        u0: Float,
        u1: Float,
        v0: Float,
        v1: Float,
        color: Color,
        renderPipeline: RenderPipeline = RenderPipelines.GUI_TEXTURE_COLOR,
    ) {
        //#if MC>=12106
        //$$ (mcContext as GuiGraphicsAccessor).plasmovoice_innerBlit(
        //$$     renderPipeline.mcRenderPipeline,
        //$$     textureLocation,
        //$$     x0,
        //$$     x1,
        //$$     y0,
        //$$     y1,
        //$$     u0,
        //$$     u1,
        //$$     v0,
        //$$     v1,
        //$$     ARGB.color(color.alpha, color.red, color.green, color.blue),
        //$$ )
        //#else
        RenderUtil.bindTexture(0, textureLocation)
        RenderUtil.blitColorWithPipeline(
            stack,
            renderPipeline,
            x0, x1, y0, y1, 0, u0, u1, v0, v1, color.red, color.green, color.blue, color.alpha
        )
        //#endif
    }

    @JvmOverloads
    fun blitSprite(
        sprite: GuiWidgetTexture,
        x: Int,
        y: Int,
        u: Int,
        v: Int,
        width: Int,
        height: Int,
        renderPipeline: RenderPipeline = RenderPipelines.GUI_TEXTURE,
    ) {
        //#if MC>=12106
        //$$ mcContext.blit(
        //$$     renderPipeline.mcRenderPipeline,
        //$$     sprite.location,
        //$$     x,
        //$$     y,
        //$$     (u + sprite.u).toFloat(),
        //$$     (v + sprite.v).toFloat(),
        //$$     width,
        //$$     height,
        //$$     sprite.textureWidth,
        //$$     sprite.textureHeight
        //$$ )
        //#else
        RenderUtil.bindTexture(0, sprite.location)
        RenderUtil.blitSprite(stack, sprite, x, y, u, v, width, height)
        //#endif
    }

    @JvmOverloads
    fun blitColorSprite(
        sprite: GuiWidgetTexture,
        x: Int,
        y: Int,
        u: Int,
        v: Int,
        width: Int,
        height: Int,
        color: Color,
        renderPipeline: RenderPipeline = RenderPipelines.GUI_TEXTURE_COLOR,
    ) {
        //#if MC>=12106
        //$$ mcContext.blit(
        //$$     renderPipeline.mcRenderPipeline,
        //$$     sprite.location,
        //$$     x,
        //$$     y,
        //$$     (u + sprite.u).toFloat(),
        //$$     (v + sprite.v).toFloat(),
        //$$     width,
        //$$     height,
        //$$     sprite.textureWidth,
        //$$     sprite.textureHeight,
        //$$     ARGB.color(color.alpha, color.red, color.green, color.blue),
        //$$ )
        //#else
        RenderUtil.bindTexture(0, sprite.location)
        RenderUtil.blitColorSprite(
            stack,
            renderPipeline,
            sprite,
            x,
            y,
            u,
            v,
            width,
            height,
            color.red,
            color.green,
            color.blue,
            color.alpha,
        )
        //#endif
    }

    @JvmOverloads
    fun drawString(
        text: McTextComponent,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
    ): Int = drawString(RenderUtil.getFormattedString(text), x, y, color, dropShadow)

    @JvmOverloads
    fun drawString(
        text: String,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
    ): Int {
        //#if MC>=12106
        //$$ mcContext.drawString(
        //$$     Minecraft.getInstance().font,
        //$$     text,
        //$$     x,
        //$$     y,
        //$$     color.rgb,
        //$$     dropShadow
        //$$ )
        //$$
        //$$ return RenderUtil.getStringX(text, x, dropShadow)
        //#else
        return RenderUtil.drawString(stack, text, x, y, color.rgb, dropShadow)
        //#endif
    }

    @JvmOverloads
    fun drawCenteredString(
        text: McTextComponent,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
    ): Int = drawCenteredString(RenderUtil.getFormattedString(text), x, y, color, dropShadow)

    @JvmOverloads
    fun drawCenteredString(
        text: String,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
    ): Int = drawString(
        text,
        x - RenderUtil.getStringWidth(text) / 2,
        y,
        color,
        dropShadow,
    )

    @JvmOverloads
    fun drawStringMultiLineCentered(
        text: McTextComponent,
        width: Int,
        y: Int,
        yGap: Int,
        color: Color,
        dropShadow: Boolean = true,
    ): Int {
        val formattedText = RenderUtil.getFormattedString(text)

        val lines = getStringSplitToWidth(formattedText, width.toFloat(), true, true)
        val lineHeight = RenderUtil.getFontHeight()

        lines.forEachIndexed { index, lineText ->
            val lineY = (lineHeight + yGap) * index

            drawString(
                lineText,
                width / 2 - RenderUtil.getStringWidth(lineText) / 2,
                y + lineY + lineHeight,
                color,
                dropShadow
            )
        }

        return lines.size
    }

    @JvmOverloads
    fun drawStringMultiLine(
        text: McTextComponent,
        x: Int,
        y: Int,
        color: Color,
        width: Int,
        dropShadow: Boolean = true,
    ): Int  {
        val formattedText = RenderUtil.getFormattedString(text)

        val lines = getStringSplitToWidth(formattedText, width.toFloat(), true, true)
        val lineHeight = RenderUtil.getFontHeight()

        lines.forEachIndexed { index, lineText ->
            val lineY = lineHeight * index

            drawString(
                lineText,
                x,
                y + lineY + lineHeight - 1,
                color,
                dropShadow
            )
        }

        return lines.size
    }

    @JvmOverloads
    fun drawOrderedString(
        text: McTextComponent,
        width: Int,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
    ) {
        drawString(
            RenderUtil.getOrderedString(text, width),
            x,
            y,
            color,
            dropShadow
        )
    }

    @JvmOverloads
    fun drawCenteredOrderedString(
        text: McTextComponent,
        width: Int,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
    ) {
        drawCenteredString(
            RenderUtil.getOrderedString(text, width),
            x,
            y,
            color,
            dropShadow
        )
    }

    @JvmOverloads
    fun fill(
        x0: Int,
        y0: Int,
        x1: Int,
        y1: Int,
        color: Color,
        renderPipeline: RenderPipeline = RenderPipelines.GUI_COLOR,
    ) {
        //#if MC>=12106
        //$$ mcContext.fill(renderPipeline.mcRenderPipeline, x0, y0, x1, y1, color.rgb)
        //#else
        RenderUtil.fill(stack, renderPipeline, x0, y0, x1, y1, color.rgb)
        //#endif
    }

    @JvmOverloads
    fun fillGradient(
        startX: Int, startY: Int, endX: Int, endY: Int,
        startRed: Int, startBlue: Int, startGreen: Int, startAlpha: Int,
        endRed: Int, endBlue: Int, endGreen: Int, endAlpha: Int,
        z: Int,
        renderPipeline: RenderPipeline = RenderPipelines.GUI_COLOR,
    ) {
        // not used anyway in 1.20+
        //#if MC<12106
        RenderUtil.fillGradientWithPipeline(stack, renderPipeline, startX, startY, endX, endY, startRed, startBlue, startGreen, startAlpha, endRed, endBlue, endGreen, endAlpha, z)
        //#endif
    }

    //#if MC>=12109
    //$$ fun requestCursor(cursorType: CursorType) {
    //$$     mcContext.requestCursor(cursorType)
    //$$ }
    //#endif
}

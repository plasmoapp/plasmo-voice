package su.plo.lib.mod.client.render.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture
import su.plo.lib.mod.client.render.Colors
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.ScissorState
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.client.mixin.accessor.GuiGraphicsAccessor
import java.awt.Color

//#if MC>=12109
//$$ import com.mojang.blaze3d.platform.cursor.CursorType
//#endif

//#if MC>=12106
//$$ import org.joml.Matrix3x2fStack
//$$ import net.minecraft.client.Minecraft
//#else
import com.mojang.blaze3d.vertex.PoseStack
//#endif

//#if MC<1.21.2
import com.mojang.blaze3d.systems.RenderSystem
//#endif

class GuiRenderContext(
    val mcContext: GuiGraphics,
) {
    //#if MC>=12106
    //$$ val stack: Matrix3x2fStack
    //$$     get() = mcContext.pose()
    //#else
    val stack: PoseStack
        get() = mcContext.pose()
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
        //$$ // I don't understand why, but mojang subtracts x and y from width and height respectively
        //$$     mcContext.enableScissor(state.x, state.y, state.width + state.x, state.height + state.y)
        //$$ } else {
        //$$     mcContext.disableScissor()
        //$$ }
        //#else
        mcContext.flush()
        ScissorState.applyScissorState(scissorStack.lastOrNull())
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
        color: Color = Colors.WHITE,
        pipeline: GuiTexturePipeline = GuiPipelines.TEXTURE,
    ) = blit(
        textureLocation,
        x,
        y,
        width,
        height,
        u,
        v,
        width,
        height,
        textureWidth,
        textureHeight,
        color,
        pipeline,
    )

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
        color: Color = Colors.WHITE,
        pipeline: GuiTexturePipeline = GuiPipelines.TEXTURE,
    ) = blitUv(
        textureLocation,
        x,
        x + width,
        y,
        y + height,
        u / textureWidth,
        (u + regionWidth) / textureWidth,
        v / textureHeight,
        (v + regionHeight) / textureHeight,
        color,
        pipeline,
    )

    @JvmOverloads
    fun blitUv(
        textureLocation: ResourceLocation,
        x0: Int,
        x1: Int,
        y0: Int,
        y1: Int,
        u0: Float,
        u1: Float,
        v0: Float,
        v1: Float,
        color: Color = Colors.WHITE,
        pipeline: GuiTexturePipeline = GuiPipelines.TEXTURE,
    ) {
        val accessor = mcContext as GuiGraphicsAccessor
        //#if MC>=1.21.2
        //$$ accessor.plasmovoice_innerBlit(pipeline.mc, textureLocation, x0, x1, y0, y1, u0, u1, v0, v1, color.rgb)
        //#else
        val renderType = pipeline.mc?.apply(textureLocation)
        if (renderType != null) {
            val pose = stack.last().pose()
            val buffer = mcContext.bufferSource().getBuffer(renderType)

            buffer.addVertex(pose, x0.toFloat(), y0.toFloat(), 0f).setUv(u0, v0).setColor(color.rgb)
            buffer.addVertex(pose, x0.toFloat(), y1.toFloat(), 0f).setUv(u0, v1).setColor(color.rgb)
            buffer.addVertex(pose, x1.toFloat(), y1.toFloat(), 0f).setUv(u1, v1).setColor(color.rgb)
            buffer.addVertex(pose, x1.toFloat(), y0.toFloat(), 0f).setUv(u1, v0).setColor(color.rgb)

            mcContext.bufferSource().endBatch(renderType)
            return
        }

        if (!pipeline.depthTest) RenderSystem.disableDepthTest()
        accessor.plasmovoice_innerBlit(
            textureLocation,
            x0, x1, y0, y1, 0,
            u0, u1, v0, v1,
            color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f,
        )
        if (!pipeline.depthTest) RenderSystem.enableDepthTest()
        //#endif
    }

    fun blitSprite(
        sprite: GuiWidgetTexture,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        //#if MC>=1.21.2
        //$$ mcContext.blitSprite(GuiPipelines.TEXTURE.mc, sprite.location, x, y, width, height)
        //#else
        RenderSystem.enableBlend()
        mcContext.blitSprite(sprite.location, x, y, width, height)
        RenderSystem.disableBlend()
        //#endif
    }

    @JvmOverloads
    fun drawString(
        text: McTextComponent,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
        seeThrough: Boolean = false,
    ): Int = drawString(RenderUtil.getVisualOrderText(text), x, y, color, dropShadow, seeThrough)

    @JvmOverloads
    fun drawString(
        text: FormattedCharSequence,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
        seeThrough: Boolean = false,
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
        return RenderUtil.drawString(stack, text, x, y, color.rgb, dropShadow, seeThrough)
        //#endif
    }

    @JvmOverloads
    fun drawString(
        text: String,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
        seeThrough: Boolean = false,
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
        return RenderUtil.drawString(stack, text, x, y, color.rgb, dropShadow, seeThrough)
        //#endif
    }

    @JvmOverloads
    fun drawCenteredString(
        text: McTextComponent,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
    ): Int = drawCenteredString(RenderUtil.getVisualOrderText(text), x, y, color, dropShadow)

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
    fun drawCenteredString(
        text: FormattedCharSequence,
        x: Int,
        y: Int,
        color: Color,
        dropShadow: Boolean = true,
    ): Int = drawString(
        text,
        x - RenderUtil.getTextWidth(text) / 2,
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
        val lines = RenderUtil.splitLines(text, width)
        val lineHeight = RenderUtil.getFontHeight()

        lines.forEachIndexed { index, line ->
            val lineY = (lineHeight + yGap) * index

            drawCenteredString(
                line,
                width / 2,
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
        val lines = RenderUtil.splitLines(text, width)
        val lineHeight = RenderUtil.getFontHeight()

        lines.forEachIndexed { index, line ->
            val lineY = lineHeight * index

            drawString(
                line,
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
            RenderUtil.getTruncatedText(text, width),
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
            RenderUtil.getTruncatedText(text, width),
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
        pipeline: GuiColorPipeline = GuiPipelines.COLOR,
    ) {
        mcContext.fill(pipeline.mc, x0, y0, x1, y1, color.rgb)
    }

    fun fillGradient(
        x0: Int,
        y0: Int,
        x1: Int,
        y1: Int,
        startColor: Color,
        endColor: Color,
    ) {
        mcContext.fillGradient(x0, y0, x1, y1, startColor.rgb, endColor.rgb)
    }

    //#if MC>=12109
    //$$ fun requestCursor(cursorType: CursorType) {
    //$$     mcContext.requestCursor(cursorType)
    //$$ }
    //#endif

    fun requestCursor(cursor: GuiCursor) {
        //#if MC>=12109
        //$$ mcContext.requestCursor(cursor.mc())
        //#endif
    }
}

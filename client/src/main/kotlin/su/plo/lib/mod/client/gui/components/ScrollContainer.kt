package su.plo.lib.mod.client.gui.components

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget
import su.plo.lib.mod.client.render.Colors
import su.plo.lib.mod.client.render.ScissorState
import su.plo.lib.mod.client.render.gui.GuiCursor
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import java.awt.Color
import kotlin.math.max

open class ScrollContainer(
    private val scrollStep: Int,
) : GuiAbstractWidget(0, 0, 0, 0),
    WidgetContainer {
    var content: GuiAbstractWidget? = null
        set(value) {
            field?.applyFocus(false)
            field = value
        }

    override val widgets: List<GuiAbstractWidget>
        get() = listOfNotNull(content)

    override val focusedChild: GuiAbstractWidget?
        get() = content?.takeIf { it.isFocused }

    var scrollTop: Double = 0.0
        set(value) {
            field = value.coerceIn(0.0, maxScroll.toDouble())
        }

    val maxScroll: Int
        get() = max(0, (content?.height ?: 0) - height)

    val viewportHeight: Int
        get() = height

    val focusedWidget: GuiAbstractWidget?
        get() = focusedLeaf()?.takeIf { it.isFocused }

    protected open val trackBackground: Boolean = true

    var isScrolling = false
        private set

    override fun render(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
    ) {
        hovered = isHovered(mouseX.toDouble(), mouseY.toDouble())
        place()

        renderContent(context, mouseX, mouseY, delta)
        renderScrollbar(context)

        if (isScrolling) {
            context.requestCursor(GuiCursor.RESIZE_NS)
        } else if (isMouseOverScrollbar(mouseX.toDouble(), mouseY.toDouble())) {
            context.requestCursor(GuiCursor.POINTING_HAND)
        }
    }

    override fun mouseClicked(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean {
        isScrolling = button == InputConstants.MOUSE_BUTTON_LEFT && isMouseOverScrollbar(mouseX, mouseY)
        if (isScrolling) return true

        val content = content ?: return false
        val inside = isHovered(mouseX, mouseY)
        val clicked =
            if (inside) {
                content.mouseClicked(mouseX, mouseY, button)
            } else {
                focusedLeaf()?.takeIf { it.capturesInput() }?.mouseClicked(mouseX, mouseY, button) ?: false
            }

        if (!clicked) {
            if (!inside) content.applyFocus(false)
            return false
        }

        focusedWidget?.let(::scrollIntoView)
        return true
    }

    override fun mouseReleased(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean {
        if (isScrolling) {
            isScrolling = false
            return true
        }

        return content?.mouseReleased(mouseX, mouseY, button) ?: false
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        deltaX: Double,
        deltaY: Double,
    ): Boolean {
        if (button != InputConstants.MOUSE_BUTTON_LEFT || !isScrolling) {
            return content?.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) ?: false
        }

        when {
            mouseY < y -> scrollTop = 0.0
            mouseY > y + height -> scrollTop = maxScroll.toDouble()
            else -> {
                val maxScroll = max(1, maxScroll).toDouble()
                val thumbHeight = Mth.clamp(height * height / max(1, content?.height ?: 0), MIN_THUMB_HEIGHT, height)
                val multiplier = max(1.0, maxScroll / max(1, height - thumbHeight).toDouble())

                scrollTop += deltaY * multiplier
            }
        }

        return true
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        delta: Double,
    ): Boolean {
        if (content?.mouseScrolled(mouseX, mouseY, delta) == true) return true
        if (maxScroll == 0) return false

        scrollTop -= delta * scrollStep

        return true
    }

    override fun keyPressed(
        keyCode: Int,
        modifiers: Int,
    ): Boolean = content?.keyPressed(keyCode, modifiers) ?: false

    override fun charTyped(
        typedChar: Char,
        modifiers: Int,
    ): Boolean = content?.charTyped(typedChar, modifiers) ?: false

    override fun keyReleased(
        keyCode: Int,
        typedChar: Char,
        modifiers: Int,
    ): Boolean = content?.keyReleased(keyCode, typedChar, modifiers) ?: false

    override fun changeFocus(lookForwards: Boolean): Boolean {
        val changed = content?.changeFocus(lookForwards) ?: false
        if (changed) focusedWidget?.let(::scrollIntoView)

        return changed
    }

    override fun capturesInput(): Boolean = content?.capturesInput() ?: false

    override fun applyFocus(focused: Boolean) {
        if (!focused) content?.applyFocus(false)
    }

    override fun isMouseOver(
        mouseX: Double,
        mouseY: Double,
    ): Boolean = isHovered(mouseX, mouseY)

    override fun isFocused(): Boolean = content?.isFocused ?: false

    override fun place() {
        val content = content ?: return

        content.measure(width)
        scrollTop = scrollTop
        content.place(x, y - scrollTop.toInt())
    }

    override fun render(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
        clipTop: Int,
        clipBottom: Int,
    ) = render(context, mouseX, mouseY, delta)

    override fun focus(target: GuiAbstractWidget): Boolean {
        val content = content ?: return false
        if (content === target) {
            content.applyFocus(true)
            return true
        }

        return (content as? WidgetContainer)?.focus(target) ?: false
    }

    fun getContentOffset(widget: GuiAbstractWidget): Int {
        place()
        return widget.y - (content?.y ?: 0)
    }

    protected open fun scrollbarX(): Int = x + width + 13

    protected fun isMouseOverScrollbar(
        mouseX: Double,
        mouseY: Double,
    ): Boolean =
        maxScroll > 0 &&
                mouseX >= scrollbarX() &&
                mouseX < scrollbarX() + SCROLLBAR_WIDTH &&
                mouseY >= y &&
                mouseY <= y + height

    private fun scrollIntoView(widget: GuiAbstractWidget) {
        place()

        when {
            widget.y < y -> scrollTop -= y - widget.y
            widget.y + widget.height > y + height -> scrollTop += widget.y + widget.height - (y + height)
        }
    }

    private fun renderContent(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
    ) {
        val content = content ?: return
        val parentScissor = context.getScissorState()

        //#if MC>=12106
        //$$ var scissor = ScissorState.ofScaled(x, y, width, height)
        //#else
        var scissor = ScissorState.of(x, Minecraft.getInstance().window.guiScaledHeight - (y + height), width, height)
        //#endif

        if (parentScissor != null) {
            val clippedX = max(scissor.x, parentScissor.x)
            val clippedY = max(scissor.y, parentScissor.y)
            val clippedRight = minOf(scissor.x + scissor.width, parentScissor.x + parentScissor.width)
            val clippedTop = minOf(scissor.y + scissor.height, parentScissor.y + parentScissor.height)

            scissor =
                ScissorState.ofScaled(
                    clippedX,
                    clippedY,
                    max(0, clippedRight - clippedX),
                    max(0, clippedTop - clippedY),
                )
        }

        context.applyScissorState(scissor)

        val inside = hovered && !isScrolling
        content.render(
            context,
            if (inside) mouseX else -1,
            if (inside) mouseY else -1,
            delta,
            y,
            y + height,
        )

        context.applyScissorState(null)
    }

    private fun renderScrollbar(context: GuiRenderContext) {
        val maxScroll = maxScroll
        if (maxScroll == 0) return

        val trackX0 = scrollbarX()
        val trackX1 = trackX0 + SCROLLBAR_WIDTH
        val trackY0 = y + SCROLLBAR_PADDING
        val trackY1 = y + height - SCROLLBAR_PADDING
        val trackHeight = trackY1 - trackY0

        val thumbHeight =
            Mth.clamp(
                trackHeight * trackHeight / max(1, content?.height ?: 0),
                MIN_THUMB_HEIGHT,
                trackHeight - 8,
            )
        val thumbTop = max(trackY0, scrollTop.toInt() * (trackHeight - thumbHeight) / maxScroll + trackY0)

        if (trackBackground) {
            context.fill(trackX0, trackY0, trackX1, trackY1, Colors.BLACK)
        }
        context.fill(trackX0, thumbTop, trackX1, thumbTop + thumbHeight, Colors.TEXT_MUTED)
        context.fill(trackX0, thumbTop, trackX1 - 1, thumbTop + thumbHeight - 1, THUMB_COLOR)
    }

    private companion object {
        const val SCROLLBAR_WIDTH = 6
        const val SCROLLBAR_PADDING = 4
        const val MIN_THUMB_HEIGHT = 32

        val THUMB_COLOR = Color(0xC0C0C0)
    }
}

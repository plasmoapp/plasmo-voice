package su.plo.voice.client.gui.settings.widget

import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import kotlin.math.max

class CompositeRowWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val gap: Int,
    vararg list: GuiAbstractWidget?,
) : GuiAbstractWidget(x, y, width, height) {

    private val widgets = list.filterNotNull()

    init {
        val zeroWidthElements = widgets.count { it.width == 0 }
        if (zeroWidthElements > 0) {
            val remaining = width - widgets.filter { it.width > 0 }.sumOf { it.width } - max(0, widgets.size - 1) * gap

            val growSize = remaining / zeroWidthElements
            widgets
                .filter { it.width == 0 }
                .forEach { it.width = growSize }
        }
    }

    override fun render(context: GuiRenderContext, mouseX: Int, mouseY: Int, delta: Float) {
        var x = this.x

        widgets.forEachIndexed { index, widget ->
            widget.x = x
            widget.y = y
            widget.render(context, mouseX, mouseY, delta)

            x += widget.width + gap
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        widgets.any { it.mouseClicked(mouseX, mouseY, button) }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
        widgets.any { it.mouseReleased(mouseX, mouseY, button) }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean =
        widgets.any { it.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean =
        widgets.any { it.mouseScrolled(mouseX, mouseY, delta) }

    override fun keyPressed(keyCode: Int, modifiers: Int): Boolean =
        widgets.any { it.keyPressed(keyCode, modifiers) }

    override fun charTyped(typedChar: Char, modifiers: Int): Boolean =
        widgets.any { it.charTyped(typedChar, modifiers) }

    override fun keyReleased(keyCode: Int, typedChar: Char, modifiers: Int): Boolean =
        widgets.any { it.keyReleased(keyCode, typedChar, modifiers) }

    override fun changeFocus(lookForwards: Boolean): Boolean =
        widgets.any { it.changeFocus(lookForwards) }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
        widgets.any { it.isMouseOver(mouseX, mouseY) }

    override fun setFocused(focused: Boolean) {
        widgets.forEach { it.isFocused = focused }
    }

    override fun isFocused(): Boolean =
        widgets.any { it.isFocused }
}

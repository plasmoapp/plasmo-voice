package su.plo.lib.mod.client.gui.components

import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget
import su.plo.lib.mod.client.render.gui.GuiRenderContext

abstract class CompositeWidget @JvmOverloads constructor(
    width: Int = 0,
) : GuiAbstractWidget(0, 0, width, 0),
    Measurable,
    WidgetContainer {
    abstract override val widgets: List<GuiAbstractWidget>

    final override var focusedChild: GuiAbstractWidget? = null
        private set

    protected val visibleWidgets: List<GuiAbstractWidget>
        get() = widgets.filter { it.isVisible }

    override fun render(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
    ) = render(context, mouseX, mouseY, delta, Int.MIN_VALUE, Int.MAX_VALUE)

    override fun mouseClicked(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean {
        if (!visible) return false

        val captured = focusedChild?.takeIf { it.capturesInput() }
        if (captured != null && captured.mouseClicked(mouseX, mouseY, button)) return true

        val clicked = visibleWidgets.firstOrNull { it !== captured && it.mouseClicked(mouseX, mouseY, button) }
        setFocused(clicked)

        return clicked != null
    }

    override fun mouseReleased(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean {
        if (focusedChild?.mouseReleased(mouseX, mouseY, button) == true) return true

        return visibleWidgets.any { it.isMouseOver(mouseX, mouseY) && it.mouseReleased(mouseX, mouseY, button) }
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        deltaX: Double,
        deltaY: Double,
    ): Boolean = focusedChild?.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) ?: false

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        delta: Double,
    ): Boolean {
        val captured = focusedChild?.takeIf { it.capturesInput() && it.isMouseOver(mouseX, mouseY) }
        if (captured != null && captured.mouseScrolled(mouseX, mouseY, delta)) return true

        return visibleWidgets.any {
            it !== captured && it.isMouseOver(mouseX, mouseY) && it.mouseScrolled(mouseX, mouseY, delta)
        }
    }

    override fun keyPressed(
        keyCode: Int,
        modifiers: Int,
    ): Boolean = focusedChild?.keyPressed(keyCode, modifiers) ?: false

    override fun charTyped(
        typedChar: Char,
        modifiers: Int,
    ): Boolean = focusedChild?.charTyped(typedChar, modifiers) ?: false

    override fun keyReleased(
        keyCode: Int,
        typedChar: Char,
        modifiers: Int,
    ): Boolean = focusedChild?.keyReleased(keyCode, typedChar, modifiers) ?: false

    override fun changeFocus(lookForwards: Boolean): Boolean {
        val current = focusedChild
        if (current != null && current.changeFocus(lookForwards)) return true

        val widgets = visibleWidgets
        val currentIndex = widgets.indexOf(current)
        val candidates =
            when {
                current == null && lookForwards -> widgets
                current == null -> widgets.asReversed()
                lookForwards -> widgets.drop(currentIndex + 1)
                else -> widgets.take(currentIndex).asReversed()
            }

        val next = candidates.firstOrNull { it.changeFocus(lookForwards) }
        setFocused(next)

        return next != null
    }

    override fun capturesInput(): Boolean = focusedChild?.capturesInput() ?: false

    override fun applyFocus(focused: Boolean) {
        if (!focused) setFocused(null)
    }

    override fun isMouseOver(
        mouseX: Double,
        mouseY: Double,
    ): Boolean = visible && visibleWidgets.any { it.isMouseOver(mouseX, mouseY) }

    override fun isFocused(): Boolean = focusedChild != null

    abstract override fun place()

    override fun render(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
        clipTop: Int,
        clipBottom: Int,
    ) {
        if (!visible) return

        hovered = isHovered(mouseX.toDouble(), mouseY.toDouble())
        renderBackground(context, mouseX, mouseY)

        visibleWidgets
            .filter { it.y < clipBottom && it.y + it.height > clipTop }
            .forEach { it.render(context, mouseX, mouseY, delta, clipTop, clipBottom) }
    }

    override fun focus(target: GuiAbstractWidget): Boolean {
        val child =
            widgets.firstOrNull { it === target || (it as? WidgetContainer)?.descendants()?.contains(target) == true }
                ?: return false

        (child as? WidgetContainer)?.focus(target)
        setFocused(child)

        return true
    }

    protected fun setFocused(widget: GuiAbstractWidget?) {
        if (focusedChild === widget) return

        focusedChild?.applyFocus(false)
        focusedChild = widget
        widget?.applyFocus(true)
    }
}

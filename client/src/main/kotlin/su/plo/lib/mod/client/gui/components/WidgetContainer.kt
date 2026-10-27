package su.plo.lib.mod.client.gui.components

import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget
import su.plo.lib.mod.client.render.gui.GuiRenderContext

interface WidgetContainer {
    val widgets: List<GuiAbstractWidget>

    val focusedChild: GuiAbstractWidget?

    fun place()

    fun render(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
        clipTop: Int,
        clipBottom: Int,
    )

    fun focus(target: GuiAbstractWidget): Boolean

    fun descendants(): List<GuiAbstractWidget> =
        widgets.flatMap { listOf(it) + (it as? WidgetContainer)?.descendants().orEmpty() }

    fun focusedLeaf(): GuiAbstractWidget? =
        generateSequence(focusedChild) { (it as? WidgetContainer)?.focusedChild }.lastOrNull()
}

fun GuiAbstractWidget.place(
    x: Int,
    y: Int,
) {
    this.x = x
    this.y = y
    (this as? WidgetContainer)?.place()
}

fun GuiAbstractWidget.render(
    context: GuiRenderContext,
    mouseX: Int,
    mouseY: Int,
    delta: Float,
    clipTop: Int,
    clipBottom: Int,
) {
    if (this is WidgetContainer) {
        render(context, mouseX, mouseY, delta, clipTop, clipBottom)
    } else {
        render(context, mouseX, mouseY, delta)
    }
}

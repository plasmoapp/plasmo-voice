package su.plo.lib.mod.client.gui.components

import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget

class CompositeColumnWidget @JvmOverloads constructor(
    private val gap: Int,
    private val paddingTop: Int = 0,
    private val paddingBottom: Int = 0,
) : CompositeWidget() {
    override var widgets: List<GuiAbstractWidget> = emptyList()
        set(value) {
            if (focusedChild !in value) setFocused(null)
            field = value
        }

    override fun measure() {
        val widgets = visibleWidgets
        widgets.forEach { it.measure(width) }

        val gaps = gap * (widgets.size - 1).coerceAtLeast(0)
        height = paddingTop + widgets.sumOf { it.height } + gaps + paddingBottom
    }

    override fun place() {
        var widgetY = y + paddingTop
        visibleWidgets.forEach {
            it.place(x, widgetY)
            widgetY += it.height + gap
        }
    }

    fun <W : GuiAbstractWidget> add(widget: W): W {
        widgets = widgets + widget
        return widget
    }
}

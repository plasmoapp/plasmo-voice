package su.plo.lib.mod.client.gui.components

import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget

class CompositeRowWidget(
    private val gap: Int,
    width: Int,
    vararg widgets: GuiAbstractWidget?,
) : CompositeWidget(width) {
    override val widgets: List<GuiAbstractWidget> = widgets.filterNotNull()

    private val growing = this.widgets.filter { it.width == 0 }.toSet()

    constructor(
        gap: Int,
        vararg widgets: GuiAbstractWidget?,
    ) : this(gap, 0, *widgets)

    override fun measure() {
        val widgets = visibleWidgets
        val (grow, fixed) = widgets.partition { it in growing }

        if (grow.isNotEmpty()) {
            val gaps = gap * (widgets.size - 1)
            val available = width - fixed.sumOf { it.width } - gaps
            val growWidth = available / grow.size

            grow.forEachIndexed { index, widget ->
                val remainder = if (index == grow.lastIndex) available - growWidth * grow.size else 0
                widget.measure(growWidth + remainder)
            }
        }

        fixed.forEach { it.measure(it.width) }

        height = widgets.maxOfOrNull { it.height } ?: 0
    }

    override fun place() {
        var widgetX = x

        visibleWidgets.forEach {
            it.place(widgetX, y + (height - it.height) / 2)
            widgetX += it.width + gap
        }
    }
}

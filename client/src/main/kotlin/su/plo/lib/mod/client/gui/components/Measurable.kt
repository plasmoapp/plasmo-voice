package su.plo.lib.mod.client.gui.components

import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget

interface Measurable {
    fun measure()
}

fun GuiAbstractWidget.measure(width: Int) {
    if (this.width != width) this.width = width
    (this as? Measurable)?.measure()
}

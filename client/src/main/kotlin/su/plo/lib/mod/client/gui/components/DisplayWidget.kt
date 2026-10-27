package su.plo.lib.mod.client.gui.components

import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget

abstract class DisplayWidget(
    width: Int,
    height: Int,
) : GuiAbstractWidget(0, 0, width, height) {
    override fun mouseClicked(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean = false

    override fun mouseReleased(
        mouseX: Double,
        mouseY: Double,
        button: Int,
    ): Boolean = false

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        deltaX: Double,
        deltaY: Double,
    ): Boolean = false

    override fun changeFocus(lookForwards: Boolean): Boolean = false

    override fun isMouseOver(
        mouseX: Double,
        mouseY: Double,
    ): Boolean = false
}

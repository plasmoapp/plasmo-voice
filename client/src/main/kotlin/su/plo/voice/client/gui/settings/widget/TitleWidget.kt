package su.plo.voice.client.gui.settings.widget

import su.plo.lib.mod.client.gui.components.CompositeWidget
import su.plo.lib.mod.client.gui.components.TextWidget
import su.plo.lib.mod.client.gui.components.measure
import su.plo.lib.mod.client.gui.components.place
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget
import su.plo.slib.api.chat.component.McTextComponent

class TitleWidget @JvmOverloads constructor(
    title: McTextComponent,
    private val leading: List<GuiAbstractWidget> = emptyList(),
    private val trailing: List<GuiAbstractWidget> = emptyList(),
) : CompositeWidget() {
    private val label = TextWidget(title, centered = true)

    override val widgets: List<GuiAbstractWidget> = buildList {
        add(label)
        addAll(leading)
        addAll(trailing)
    }

    override fun measure() {
        label.measure(width)
        (leading + trailing).forEach { it.measure(it.width) }
        height = maxOf(HEIGHT, widgets.maxOf { it.height })
    }

    override fun place() {
        label.place(x, centeredY(label))

        var leadingX = x
        leading.forEach {
            it.place(leadingX, centeredY(it))
            leadingX += it.width + GAP
        }

        var trailingX = x + width
        trailing.asReversed().forEach {
            trailingX -= it.width
            it.place(trailingX, centeredY(it))
            trailingX -= GAP
        }
    }

    private fun centeredY(widget: GuiAbstractWidget): Int =
        y + (height - widget.height) / 2

    private companion object {
        const val HEIGHT = 20
        const val GAP = 4
    }
}

package su.plo.lib.mod.client.gui

import net.minecraft.util.FormattedCharSequence
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.slib.api.chat.component.McTextComponent

data class TooltipData(
    val text: List<McTextComponent>,
    val x: Int,
    val y: Int,
) {
    constructor(
        text: McTextComponent,
        x: Int,
        y: Int,
    ) : this(listOf(text), x, y)

    val lines: List<FormattedCharSequence>
        get() = text.flatMap { RenderUtil.splitLines(it, MAX_WIDTH) }

    companion object {
        private const val MAX_WIDTH = 180
    }
}

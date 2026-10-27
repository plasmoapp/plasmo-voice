package su.plo.lib.mod.client.gui.components

import su.plo.lib.mod.client.render.Colors
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.client.extension.getStringSplitToWidth
import java.awt.Color

class MultilineTextWidget @JvmOverloads constructor(
    private val text: McTextComponent,
    private val color: Color = Colors.WHITE,
) : DisplayWidget(0, 0),
    Measurable {
    private var wrappedWidth = -1
    private var lines: List<String> = emptyList()

    override fun renderButton(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
    ) {
        lines.forEachIndexed { index, line ->
            context.drawString(line, x, y + index * RenderUtil.getFontHeight(), color)
        }
    }

    override fun measure() {
        if (width != wrappedWidth) {
            wrappedWidth = width
            lines = getStringSplitToWidth(
                RenderUtil.getFormattedString(text),
                width.toFloat(),
                ensureSpaceAtEndOfLines = true,
                processColorCodes = true,
            )
        }

        height = (lines.size * RenderUtil.getFontHeight() - 1).coerceAtLeast(0)
    }
}

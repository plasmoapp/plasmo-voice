package su.plo.lib.mod.client.gui.components

import net.minecraft.util.FormattedCharSequence
import su.plo.lib.mod.client.render.Colors
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import su.plo.slib.api.chat.component.McTextComponent
import java.awt.Color

class MultilineTextWidget @JvmOverloads constructor(
    private val texts: List<McTextComponent>,
    private val color: Color = Colors.WHITE,
    private val centered: Boolean = false,
) : DisplayWidget(0, 0),
    Measurable {
    @JvmOverloads
    constructor(
        text: McTextComponent,
        color: Color = Colors.WHITE,
        centered: Boolean = false,
    ) : this(listOf(text), color, centered)

    private var wrappedWidth = -1
    private var lines: List<FormattedCharSequence> = emptyList()

    override fun renderButton(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
    ) {
        lines.forEachIndexed { index, line ->
            val lineY = y + index * RenderUtil.getFontHeight()

            if (centered) {
                context.drawCenteredString(line, x + width / 2, lineY, color)
            } else {
                context.drawString(line, x, lineY, color)
            }
        }
    }

    override fun measure() {
        if (width != wrappedWidth) {
            wrappedWidth = width
            lines = texts.flatMap { RenderUtil.splitLines(it, width) }
        }

        height = (lines.size * RenderUtil.getFontHeight() - 1).coerceAtLeast(0)
    }
}

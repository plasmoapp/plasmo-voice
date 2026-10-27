package su.plo.lib.mod.client.gui.components

import su.plo.lib.mod.client.render.Colors
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import su.plo.slib.api.chat.component.McTextComponent
import java.awt.Color
import java.util.function.Supplier

class TextWidget @JvmOverloads constructor(
    private val text: Supplier<McTextComponent?>,
    private val color: Color = Colors.WHITE,
    private val centered: Boolean = false,
    width: Int = 0,
) : DisplayWidget(width, TEXT_HEIGHT) {
    @JvmOverloads
    constructor(
        text: McTextComponent,
        color: Color = Colors.WHITE,
        centered: Boolean = false,
        width: Int = 0,
    ) : this(Supplier { text }, color, centered, width)

    override fun renderButton(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
    ) {
        val text = text.get() ?: return

        if (centered) {
            context.drawCenteredOrderedString(text, width, x + width / 2, y, color)
        } else {
            context.drawOrderedString(text, width, x, y, color)
        }
    }

    companion object {
        const val TEXT_HEIGHT = 8
    }
}

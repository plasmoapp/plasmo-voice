package su.plo.voice.client.gui.settings.widget

import net.minecraft.client.Minecraft
import su.plo.lib.mod.client.Inputs
import su.plo.lib.mod.client.gui.components.CompositeColumnWidget
import su.plo.lib.mod.client.gui.components.ScrollContainer
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget
import su.plo.lib.mod.client.render.Colors
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.client.gui.settings.VoiceSettingsScreen
import java.awt.Color
import java.util.function.Consumer
import kotlin.math.min

class DropDownWidgetList(
    private val dropDownWidget: DropDownWidget,
    private val elements: List<McTextComponent>,
    private val parent: VoiceSettingsScreen,
    private val enableTooltip: Boolean,
    private val onSelect: Consumer<Int>,
) : ScrollContainer(ELEMENT_HEIGHT) {
    override val trackBackground: Boolean = false

    init {
        content = CompositeColumnWidget(0).apply {
            elements.forEachIndexed { index, element -> add(Item(index, element)) }
        }
    }

    override fun render(context: GuiRenderContext, mouseX: Int, mouseY: Int, delta: Float) {
        x = dropDownWidget.x
        width = dropDownWidget.width
        if (shouldRenderToTop()) {
            height = ELEMENT_HEIGHT * min(elements.size, VISIBLE_ELEMENTS)
            y = dropDownWidget.y - height
        } else {
            y = dropDownWidget.y + dropDownWidget.height
            height = min(ELEMENT_HEIGHT * VISIBLE_ELEMENTS, Minecraft.getInstance().window.guiScaledHeight - y)
        }

        parent.deferredRender {
            //#if MC<12106
            context.stack.pushPose()
            context.stack.translate(0.0, 0.0, 10.0)
            //#endif

            super.render(context, parent.mouseX, parent.mouseY, delta)

            if (maxScroll > 0) {
                val lineY = if (shouldRenderToTop()) y else y + height

                context.fill(
                    x,
                    lineY - 1,
                    x + width,
                    lineY,
                    frameColor,
                )
            }

            //#if MC<12106
            context.stack.popPose()
            //#endif
        }
    }

    override fun scrollbarX(): Int =
        x + width - 11

    private fun shouldRenderToTop(): Boolean {
        val scaledHeight = Minecraft.getInstance().window.guiScaledHeight

        val maxListHeight = ELEMENT_HEIGHT * min(elements.size, VISIBLE_ELEMENTS)

        val dropDownFullHeightYDown = dropDownWidget.y + dropDownWidget.height + 1 + maxListHeight
        val dropDownFullHeightYUp = dropDownWidget.y - 1 - maxListHeight
        val navBottom = parent.navigation.height

        return dropDownFullHeightYDown > scaledHeight && dropDownFullHeightYUp > navBottom
    }

    private inner class Item(
        private val index: Int,
        private val element: McTextComponent,
    ) : GuiAbstractWidget(0, 0, 0, ELEMENT_HEIGHT) {

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (!isMouseOver(mouseX, mouseY)) return false

            onSelect.accept(index)
            return true
        }

        override fun renderButton(context: GuiRenderContext, mouseX: Int, mouseY: Int, delta: Float) {
            val yOffset = if (shouldRenderToTop()) 1 else 0

            context.fill(x, y, x + width, y + height, frameColor)
            context.fill(x + 1, y + yOffset, x + width - 1, y + height - 1 + yOffset, Colors.BLACK)

            val hasScroll = maxScroll > 0
            val entryPaddingRight = if (hasScroll) 23 else 10

            if (hovered || isFocused) {
                if (enableTooltip && RenderUtil.getTextWidth(element) > (width - entryPaddingRight) &&
                    !isMouseOverScrollbar(mouseX.toDouble(), mouseY.toDouble())
                ) {
                    if (hovered) {
                        parent.setTooltip(element, mouseX, mouseY)
                    } else if (Inputs.isLastInputKeyboard()) {
                        parent.setTooltip(element, x, y + height * 2)
                    }
                }
                context.fill(x + 1, y + yOffset, x + width - 1, y + height - 1 + yOffset, hoverColor)
            }

            context.drawOrderedString(
                element,
                width - entryPaddingRight,
                x + 5,
                y + height / 2 - RenderUtil.getFontHeight() / 2 + yOffset,
                Colors.TEXT
            )
        }
    }

    companion object {
        private const val ELEMENT_HEIGHT = 17
        private const val VISIBLE_ELEMENTS = 5

        private val frameColor = Color(0x464646)
        private val hoverColor = Color(0x323232)
    }
}

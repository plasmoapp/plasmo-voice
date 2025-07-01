package su.plo.voice.client.gui.settings.widget

import net.minecraft.client.Minecraft
import su.plo.lib.mod.client.gui.components.AbstractScrollbar
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
    parent: VoiceSettingsScreen,
    width: Int,
    private val enableTooltip: Boolean,
    private val onSelect: Consumer<Int>,
) : AbstractScrollbar<VoiceSettingsScreen>(parent, width, 0, 0) {

    init {
        init()
    }

    override fun init() {
        clearEntries()
        elements.mapIndexed(::Entry).forEach(::addEntry)
    }

    override fun render(context: GuiRenderContext, mouseX: Int, mouseY: Int, delta: Float) {
        val windowHeight = Minecraft.getInstance().window.guiScaledHeight
        if (shouldRenderToTop()) {
            this.y1 = dropDownWidget.y
            this.y0 = y1 - (elementHeight * min(elements.size, maxElements))
        } else {
            this.y0 = dropDownWidget.y + dropDownWidget.height
            this.y1 = min(y0 + (elementHeight * maxElements), windowHeight)
        }

        parent.deferredRender {
            //#if MC<12106
            context.stack.pushPose()
            context.stack.translate(0.0, 0.0, 10.0)
            //#endif

            super.render(context, mouseX, mouseY, delta)

            if (maxScroll > 0) {
                val lineY = if (shouldRenderToTop()) y0 else y1

                context.fill(
                    containerX0,
                    lineY - 1,
                    containerX1,
                    lineY,
                    Color(0x464646),
                )
            }

            //#if MC<12106
            context.stack.pushPose()
            context.stack.translate(0.0, 0.0, 10.0)
            //#endif
        }
    }

    override fun getContainerX0(): Int =
        dropDownWidget.x

    override fun getScrollbarPosition(): Int =
        containerX1 - 11

    override fun shouldRenderScrollbarBackground(): Boolean =
        false

    private fun shouldRenderToTop(): Boolean {
        val scaledHeight = Minecraft.getInstance().window.guiScaledHeight

        val maxListHeight = elementHeight * min(elements.size, maxElements)

        val dropDownFullHeightYDown = dropDownWidget.y + dropDownWidget.height + 1 + maxListHeight
        val dropDownFullHeightYUp = dropDownWidget.y - 1 - maxListHeight
        val navBottom = parent.navigation.height

        return dropDownFullHeightYDown > scaledHeight && dropDownFullHeightYUp > navBottom
    }

    inner class Entry(
        private val index: Int,
        private val element: McTextComponent,
    ) : AbstractScrollbar<VoiceSettingsScreen>.Entry(elementHeight) {

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            onSelect.accept(index)
            return true
        }

        override fun render(
            context: GuiRenderContext,
            index: Int,
            x: Int,
            y: Int,
            entryWidth: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            delta: Float,
        ) {
            val yOffset = if (shouldRenderToTop()) 1 else 0

            context.fill(x, y, x + entryWidth, y + height, Color(0x464646))
            context.fill(x + 1, y + yOffset, x + entryWidth - 1, y + height - 1 + yOffset, Colors.BLACK)

            val hasScroll = maxScroll > 0
            val entryPaddingRight = if (hasScroll) 23 else 10

            if (hovered) {
                if (enableTooltip && RenderUtil.getTextWidth(element) > (entryWidth - entryPaddingRight) &&
                    !isMouseOverScrollbar(mouseX.toDouble(), mouseY.toDouble())
                ) {
                    parent.setTooltip(element)
                }
                context.fill(x + 1, y + yOffset, x + entryWidth - 1, y + height - 1 + yOffset, Color(0x323232))
            }

            context.drawOrderedString(
                    element,
                entryWidth - entryPaddingRight,
                    x + 5,
                    y + height / 2 - RenderUtil.getFontHeight() / 2 + yOffset,
                    Color(0xE0E0E0)
            )
        }
    }

    companion object {
        private val elementHeight = 17
        private val maxElements = 5
    }
}

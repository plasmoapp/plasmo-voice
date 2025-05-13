package su.plo.voice.client.gui.settings.widget

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import su.plo.lib.mod.client.gui.components.AbstractScrollbar
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.pipeline.RenderPipelines
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.client.gui.settings.VoiceSettingsScreen
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

    override fun render(stack: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        val windowHeight = Minecraft.getInstance().window.guiScaledHeight
        if (shouldRenderToTop()) {
            this.y1 = dropDownWidget.y
            this.y0 = y1 - (elementHeight * min(elements.size, maxElements))
        } else {
            this.y0 = dropDownWidget.y + dropDownWidget.height
            this.y1 = min(y0 + (elementHeight * maxElements), windowHeight)
        }

        stack.pushPose()
        stack.translate(0.0, 0.0, 10.0)

        super.render(stack, mouseX, mouseY, delta)

        if (maxScroll > 0) {
            val lineY = if (shouldRenderToTop()) y0 else y1

            RenderUtil.fill(
                stack,
                RenderPipelines.GUI_COLOR_OVERLAY,
                containerX0,
                lineY - 1,
                containerX1,
                lineY,
                -0xB9B9BA
            )
        }

        stack.popPose()
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
            stack: PoseStack,
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

            RenderUtil.fill(stack, x, y, x + entryWidth, y + height, -0xB9B9BA)
            RenderUtil.fill(stack, x + 1, y + yOffset, x + entryWidth - 1, y + height - 1 + yOffset, -0x1000000)

            val hasScroll = maxScroll > 0
            val entryPaddingRight = if (hasScroll) 23 else 10

            if (hovered) {
                if (enableTooltip && RenderUtil.getTextWidth(element) > (entryWidth - entryPaddingRight) &&
                    !isMouseOverScrollbar(mouseX.toDouble(), mouseY.toDouble())
                ) {
                    parent.setTooltip(element)
                }
                RenderUtil.fill(stack, x + 1, y + yOffset, x + entryWidth - 1, y + height - 1 + yOffset, -0xCDCDCE)
            }

            RenderUtil.drawOrderedString(
                    stack,
                    element,
                entryWidth - entryPaddingRight,
                    x + 5,
                    y + height / 2 - RenderUtil.getFontHeight() / 2 + yOffset,
                    0xE0E0E0
            )
        }
    }

    companion object {
        private val elementHeight = 17
        private val maxElements = 5
    }
}

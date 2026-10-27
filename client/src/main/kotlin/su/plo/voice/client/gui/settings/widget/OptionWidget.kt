package su.plo.voice.client.gui.settings.widget

import net.minecraft.resources.ResourceLocation
import su.plo.config.entry.ConfigEntry
import su.plo.lib.mod.client.ResourceLocationUtil
import su.plo.lib.mod.client.gui.components.Button
import su.plo.lib.mod.client.gui.components.CompositeRowWidget
import su.plo.lib.mod.client.gui.components.CompositeWidget
import su.plo.lib.mod.client.gui.components.IconButton
import su.plo.lib.mod.client.gui.components.TextWidget
import su.plo.lib.mod.client.gui.components.measure
import su.plo.lib.mod.client.gui.components.place
import su.plo.lib.mod.client.gui.screen.TooltipScreen
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget
import su.plo.lib.mod.client.render.gui.GuiRenderContext
import su.plo.slib.api.chat.component.McTextComponent

open class OptionWidget<W : GuiAbstractWidget> @JvmOverloads constructor(
    private val parent: TooltipScreen,
    label: GuiAbstractWidget,
    @JvmField protected val element: W,
    @JvmField protected val entry: ConfigEntry<*>,
    private val tooltip: McTextComponent? = null,
    @JvmField protected val resetAction: ResetAction<W>? = null,
    buttons: List<GuiAbstractWidget> = emptyList(),
) : CompositeWidget() {
    @JvmField
    protected val resetButton = IconButton(
        0,
        0,
        20,
        20,
        { onReset() },
        Button.NO_TOOLTIP,
        RESET_ICON,
        true,
    )

    private val row = CompositeRowWidget(GAP, label, element, *buttons.toTypedArray(), resetButton)

    override val widgets: List<GuiAbstractWidget> = listOf(row)

    @JvmOverloads
    constructor(
        parent: TooltipScreen,
        text: McTextComponent,
        element: W,
        entry: ConfigEntry<*>,
        tooltip: McTextComponent? = null,
        resetAction: ResetAction<W>? = null,
        buttons: List<GuiAbstractWidget> = emptyList(),
    ) : this(parent, TextWidget(text), element, entry, tooltip, resetAction, buttons)

    override fun measure() {
        row.measure(width)
        height = row.height
    }

    override fun place() {
        row.place(x, y)
    }

    override fun render(
        context: GuiRenderContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float,
        clipTop: Int,
        clipBottom: Int,
    ) {
        resetButton.isActive = !isDefault()
        super.render(context, mouseX, mouseY, delta, clipTop, clipBottom)

        if (hovered && mouseX < element.x) {
            parent.setTooltip(tooltip, mouseX, mouseY)
        }
    }

    protected open fun isDefault(): Boolean =
        entry.isDefault

    protected open fun onReset() {
        entry.reset()
        (element as? UpdatableWidget)?.updateValue()
        resetAction?.onReset(resetButton, element)
    }

    fun interface ResetAction<W : GuiAbstractWidget> {
        fun onReset(resetButton: IconButton, element: W)
    }

    private companion object {
        const val GAP = 4

        val RESET_ICON: ResourceLocation = ResourceLocationUtil.parse("plasmovoice:textures/icons/reset.png")
    }
}

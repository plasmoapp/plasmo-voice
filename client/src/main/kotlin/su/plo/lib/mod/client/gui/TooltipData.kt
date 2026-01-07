package su.plo.lib.mod.client.gui

import su.plo.lib.mod.client.render.RenderUtil
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.client.extension.getStringSplitToWidth

data class TooltipData(
    val text: List<McTextComponent>,
    val x: Int,
    val y: Int,
) {
    constructor(
        text: McTextComponent,
        x: Int,
        y: Int,
    ) : this(
        getStringSplitToWidth(
            RenderUtil.getFormattedString(text),
            180.0f,
            true,
            true,
        ).map { McTextComponent.literal(it) },
        x,
        y,
    )
}

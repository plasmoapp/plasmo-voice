package su.plo.voice.client.extension

import net.minecraft.network.chat.Component
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.slib.api.chat.component.McTextComponent

fun McTextComponent.toMinecraft(): Component = RenderUtil.getTextConverter().convert(this)

fun String.width() = RenderUtil.getStringWidth(this)

fun McTextComponent.width() = RenderUtil.getTextWidth(this)

fun List<McTextComponent>.join(delimiter: McTextComponent): McTextComponent {
    val joined = McTextComponent.empty()

    (0 until this.size).forEach { i ->
        val component = this[i]
        joined.append(component)

        if (i != this.size - 1) {
            joined.append(delimiter)
        }
    }

    return joined
}

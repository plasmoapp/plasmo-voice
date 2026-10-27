package su.plo.voice.client.gui.settings.widget

import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.gui.components.DisplayWidget
import su.plo.lib.mod.client.render.gui.GuiRenderContext

class IconWidget(
    private val icon: ResourceLocation,
) : DisplayWidget(SIZE, SIZE) {
    override fun renderButton(context: GuiRenderContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.blit(icon, x, y, 0f, 0f, SIZE, SIZE, SIZE, SIZE)
    }

    private companion object {
        const val SIZE = 16
    }
}

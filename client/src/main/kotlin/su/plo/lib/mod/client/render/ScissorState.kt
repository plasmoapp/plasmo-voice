package su.plo.lib.mod.client.render

import net.minecraft.client.Minecraft

//#if MC<1.21.6
import com.mojang.blaze3d.systems.RenderSystem
//#endif

data class ScissorState(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    companion object {
        @JvmStatic
        fun ofScaled(x: Int, y: Int, width: Int, height: Int): ScissorState =
            ScissorState(x, y, width, height)

        @JvmStatic
        fun of(x: Int, y: Int, width: Int, height: Int): ScissorState {
            val scaleFactor = Minecraft.getInstance().window.guiScale

            val scaledX = x * scaleFactor
            val scaledY = y * scaleFactor
            val scaledWidth = width * scaleFactor
            val scaledHeight = height * scaleFactor

            return ScissorState(
                scaledX.toInt(),
                scaledY.toInt(),
                scaledWidth.toInt(),
                scaledHeight.toInt(),
            )
        }

        //#if MC<1.21.6
        fun applyScissorState(state: ScissorState?) {
            if (state == null) {
                RenderSystem.disableScissor()
                return
            }

            RenderSystem.enableScissor(state.x, state.y, state.width, state.height)
        }
        //#endif
    }
}

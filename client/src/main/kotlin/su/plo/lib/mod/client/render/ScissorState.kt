package su.plo.lib.mod.client.render

import net.minecraft.client.Minecraft

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

        fun applyScissorState(state: ScissorState?) {
            if (state == null) {
                RenderUtil.disableScissor()
                return
            }

            RenderUtil.enableScissor(state.x, state.y, state.width, state.height)
        }
    }
}

package su.plo.lib.mod.client.render

import org.lwjgl.opengl.GL11

data class ScissorState(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

fun applyScissorState(state: ScissorState?) {
    if (state == null) {
        RenderUtil.disableScissor()
        return
    }

    RenderUtil.enableScissorScaled(state.x, state.y, state.width, state.height)
}

fun getScissorState(): ScissorState? {
    if (!GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) return null

    val scissorBox = IntArray(4)
    GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissorBox)

    return ScissorState(
        scissorBox[0],
        scissorBox[1],
        scissorBox[2],
        scissorBox[3],
    )
}

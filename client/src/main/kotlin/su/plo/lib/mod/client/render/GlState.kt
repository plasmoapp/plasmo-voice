package su.plo.lib.mod.client.render

//#if MC<12105
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14

data class GlState(
    val depthFunc: Int?,
    val cull: Boolean,
    val blendFunc: List<Int>?,
    val depthMask: Boolean,
    val polygonOffset: Pair<Float, Float>?
)

fun applyGlState(state: GlState) {
    if (state.depthFunc != null) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(state.depthFunc);
    } else {
        RenderSystem.disableDepthTest()
    }

    if (state.cull) {
        RenderSystem.enableCull()
    } else {
        RenderSystem.disableCull()
    }

    if (state.blendFunc != null) {
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(
            state.blendFunc[0],
            state.blendFunc[1],
            state.blendFunc[2],
            state.blendFunc[3],
        )
    } else {
        RenderSystem.disableBlend()
    }

    RenderSystem.depthMask(state.depthMask)

    if (state.polygonOffset != null) {
        RenderSystem.polygonOffset(state.polygonOffset.first, state.polygonOffset.second)
        RenderSystem.enablePolygonOffset()
    } else {
        RenderSystem.disablePolygonOffset()
    }
}

fun currentGlState(): GlState {
    val depthFunc: Int? =
        if (GL11.glIsEnabled(GL11.GL_DEPTH_TEST))
            GL11.glGetInteger(GL11.GL_DEPTH_FUNC)
        else
            null

    val cull = GL11.glIsEnabled(GL11.GL_CULL_FACE)

    val blendFunc: List<Int>? =
        if (GL11.glIsEnabled(GL11.GL_BLEND))
            listOf(
                GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),
                GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),
                GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),
            )
        else
            null

    val depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)

    val polygonOffset: Pair<Float, Float>? =
        if (GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL))
            GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_FACTOR) to GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_UNITS)
        else
            null

    return GlState(
        depthFunc,
        cull,
        blendFunc,
        depthMask,
        polygonOffset
    )
}
//#endif
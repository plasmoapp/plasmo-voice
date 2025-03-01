package su.plo.lib.mod.client.render.pipeline

import org.lwjgl.opengl.GL11C

//#if MC>=12105
//$$ import com.mojang.blaze3d.platform.DepthTestFunction
//#endif

enum class AlphaFunc {
    NEVER,
    LESS,
    EQUAL,
    LEQUAL,
    GREATER,
    NOTEQUAL,
    GEQUAL,
    ALWAYS;

    //#if MC>=12105
    //$$ fun mcDepthFunc() =
    //$$     when (this) {
    //$$         ALWAYS -> DepthTestFunction.NO_DEPTH_TEST
    //$$         EQUAL -> DepthTestFunction.EQUAL_DEPTH_TEST
    //$$         LEQUAL -> DepthTestFunction.LEQUAL_DEPTH_TEST
    //$$         LESS -> DepthTestFunction.LESS_DEPTH_TEST
    //$$         GREATER -> DepthTestFunction.GREATER_DEPTH_TEST
    //$$         else -> throw IllegalArgumentException("Unknown depth func $this")
    //$$     }
    //#endif

    fun gl() =
        when (this) {
            NEVER -> GL11C.GL_NEVER
            LESS -> GL11C.GL_LESS
            EQUAL -> GL11C.GL_EQUAL
            LEQUAL -> GL11C.GL_LEQUAL
            GREATER -> GL11C.GL_GREATER
            NOTEQUAL -> GL11C.GL_NOTEQUAL
            GEQUAL -> GL11C.GL_GEQUAL
            ALWAYS -> GL11C.GL_ALWAYS
        }
}
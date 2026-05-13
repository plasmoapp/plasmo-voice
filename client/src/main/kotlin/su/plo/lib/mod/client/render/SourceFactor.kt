package su.plo.lib.mod.client.render

import org.lwjgl.opengl.GL14C

//#if MC>=26.2
//$$ typealias BlazeSourceFactor = com.mojang.blaze3d.platform.BlendFactor
//#elseif MC>=12105
//$$ typealias BlazeSourceFactor = com.mojang.blaze3d.platform.SourceFactor
//#endif

enum class SourceFactor {
    CONSTANT_ALPHA,
    CONSTANT_COLOR,
    DST_ALPHA,
    DST_COLOR,
    ONE,
    ONE_MINUS_CONSTANT_ALPHA,
    ONE_MINUS_CONSTANT_COLOR,
    ONE_MINUS_DST_ALPHA,
    ONE_MINUS_DST_COLOR,
    ONE_MINUS_SRC_ALPHA,
    ONE_MINUS_SRC_COLOR,
    SRC_ALPHA,
    SRC_ALPHA_SATURATE,
    SRC_COLOR,
    ZERO;

    //#if MC>=12105
    //$$ fun mc() =
    //$$     when (this) {
    //$$         CONSTANT_ALPHA -> BlazeSourceFactor.CONSTANT_ALPHA
    //$$         CONSTANT_COLOR -> BlazeSourceFactor.CONSTANT_COLOR
    //$$         DST_ALPHA -> BlazeSourceFactor.DST_ALPHA
    //$$         DST_COLOR -> BlazeSourceFactor.DST_COLOR
    //$$         ONE -> BlazeSourceFactor.ONE
    //$$         ONE_MINUS_CONSTANT_ALPHA -> BlazeSourceFactor.ONE_MINUS_CONSTANT_ALPHA
    //$$         ONE_MINUS_CONSTANT_COLOR -> BlazeSourceFactor.ONE_MINUS_CONSTANT_COLOR
    //$$         ONE_MINUS_DST_ALPHA -> BlazeSourceFactor.ONE_MINUS_DST_ALPHA
    //$$         ONE_MINUS_DST_COLOR -> BlazeSourceFactor.ONE_MINUS_DST_COLOR
    //$$         ONE_MINUS_SRC_ALPHA -> BlazeSourceFactor.ONE_MINUS_SRC_ALPHA
    //$$         ONE_MINUS_SRC_COLOR -> BlazeSourceFactor.ONE_MINUS_SRC_COLOR
    //$$         SRC_ALPHA -> BlazeSourceFactor.SRC_ALPHA
    //$$         SRC_ALPHA_SATURATE -> BlazeSourceFactor.SRC_ALPHA_SATURATE
    //$$         SRC_COLOR -> BlazeSourceFactor.SRC_COLOR
    //$$         ZERO -> BlazeSourceFactor.ZERO
    //$$     }
    //#endif

    fun gl() =
        when (this) {
            CONSTANT_ALPHA -> GL14C.GL_CONSTANT_ALPHA
            CONSTANT_COLOR -> GL14C.GL_CONSTANT_COLOR
            DST_ALPHA -> GL14C.GL_DST_ALPHA
            DST_COLOR -> GL14C.GL_DST_COLOR
            ONE -> GL14C.GL_ONE
            ONE_MINUS_CONSTANT_ALPHA -> GL14C.GL_ONE_MINUS_CONSTANT_ALPHA
            ONE_MINUS_CONSTANT_COLOR -> GL14C.GL_ONE_MINUS_CONSTANT_COLOR
            ONE_MINUS_DST_ALPHA -> GL14C.GL_ONE_MINUS_DST_ALPHA
            ONE_MINUS_DST_COLOR -> GL14C.GL_ONE_MINUS_DST_COLOR
            ONE_MINUS_SRC_ALPHA -> GL14C.GL_ONE_MINUS_SRC_ALPHA
            ONE_MINUS_SRC_COLOR -> GL14C.GL_ONE_MINUS_SRC_COLOR
            SRC_ALPHA -> GL14C.GL_SRC_ALPHA
            SRC_ALPHA_SATURATE -> GL14C.GL_SRC_ALPHA_SATURATE
            SRC_COLOR -> GL14C.GL_SRC_COLOR
            ZERO -> GL14C.GL_ZERO
        }
}

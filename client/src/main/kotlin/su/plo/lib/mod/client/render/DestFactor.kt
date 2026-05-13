package su.plo.lib.mod.client.render

import org.lwjgl.opengl.GL14C

//#if MC>=26.2
//$$ typealias BlazeDestFactor = com.mojang.blaze3d.platform.BlendFactor
//#elseif MC>=12105
//$$ typealias BlazeDestFactor = com.mojang.blaze3d.platform.DestFactor
//#endif

enum class DestFactor {
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
    SRC_COLOR,
    ZERO;

    //#if MC>=12105
    //$$ fun mc() =
    //$$     when (this) {
    //$$         CONSTANT_ALPHA -> BlazeDestFactor.CONSTANT_ALPHA
    //$$         CONSTANT_COLOR -> BlazeDestFactor.CONSTANT_COLOR
    //$$         DST_ALPHA -> BlazeDestFactor.DST_ALPHA
    //$$         DST_COLOR -> BlazeDestFactor.DST_COLOR
    //$$         ONE -> BlazeDestFactor.ONE
    //$$         ONE_MINUS_CONSTANT_ALPHA -> BlazeDestFactor.ONE_MINUS_CONSTANT_ALPHA
    //$$         ONE_MINUS_CONSTANT_COLOR -> BlazeDestFactor.ONE_MINUS_CONSTANT_COLOR
    //$$         ONE_MINUS_DST_ALPHA -> BlazeDestFactor.ONE_MINUS_DST_ALPHA
    //$$         ONE_MINUS_DST_COLOR -> BlazeDestFactor.ONE_MINUS_DST_COLOR
    //$$         ONE_MINUS_SRC_ALPHA -> BlazeDestFactor.ONE_MINUS_SRC_ALPHA
    //$$         ONE_MINUS_SRC_COLOR -> BlazeDestFactor.ONE_MINUS_SRC_COLOR
    //$$         SRC_ALPHA -> BlazeDestFactor.SRC_ALPHA
    //$$         SRC_COLOR -> BlazeDestFactor.SRC_COLOR
    //$$         ZERO -> BlazeDestFactor.ZERO
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
            SRC_COLOR -> GL14C.GL_SRC_COLOR
            ZERO -> GL14C.GL_ZERO
        }
}

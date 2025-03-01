package su.plo.lib.mod.client.render

import org.lwjgl.opengl.GL14C

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
    //$$         CONSTANT_ALPHA -> com.mojang.blaze3d.platform.SourceFactor.CONSTANT_ALPHA
    //$$         CONSTANT_COLOR -> com.mojang.blaze3d.platform.SourceFactor.CONSTANT_COLOR
    //$$         DST_ALPHA -> com.mojang.blaze3d.platform.SourceFactor.DST_ALPHA
    //$$         DST_COLOR -> com.mojang.blaze3d.platform.SourceFactor.DST_COLOR
    //$$         ONE -> com.mojang.blaze3d.platform.SourceFactor.ONE
    //$$         ONE_MINUS_CONSTANT_ALPHA -> com.mojang.blaze3d.platform.SourceFactor.ONE_MINUS_CONSTANT_ALPHA
    //$$         ONE_MINUS_CONSTANT_COLOR -> com.mojang.blaze3d.platform.SourceFactor.ONE_MINUS_CONSTANT_COLOR
    //$$         ONE_MINUS_DST_ALPHA -> com.mojang.blaze3d.platform.SourceFactor.ONE_MINUS_DST_ALPHA
    //$$         ONE_MINUS_DST_COLOR -> com.mojang.blaze3d.platform.SourceFactor.ONE_MINUS_DST_COLOR
    //$$         ONE_MINUS_SRC_ALPHA -> com.mojang.blaze3d.platform.SourceFactor.ONE_MINUS_SRC_ALPHA
    //$$         ONE_MINUS_SRC_COLOR -> com.mojang.blaze3d.platform.SourceFactor.ONE_MINUS_SRC_COLOR
    //$$         SRC_ALPHA -> com.mojang.blaze3d.platform.SourceFactor.SRC_ALPHA
    //$$         SRC_ALPHA_SATURATE -> com.mojang.blaze3d.platform.SourceFactor.SRC_ALPHA_SATURATE
    //$$         SRC_COLOR -> com.mojang.blaze3d.platform.SourceFactor.SRC_COLOR
    //$$         ZERO -> com.mojang.blaze3d.platform.SourceFactor.ZERO
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
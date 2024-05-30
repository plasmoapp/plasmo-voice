package gg.essential.universal.shader

import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14

//#if MC>=11700
import com.mojang.blaze3d.shaders.BlendMode
//#endif

//#if MC>=11500
import org.lwjgl.opengl.GL20
//#endif

data class BlendState(
    val equation: Equation,
    val srcRgb: Param,
    val dstRgb: Param,
    val srcAlpha: Param = srcRgb,
    val dstAlpha: Param = dstRgb,
    val enabled: Boolean = true,
) {
    val separate = srcRgb != srcAlpha || dstRgb != dstAlpha

    //#if MC>=11700
    private inner class McBlendState : BlendMode {
        constructor() : super()
        constructor(srcRgb: Int, dstRgb: Int, func: Int) : super(srcRgb, dstRgb, func)
        constructor(srcRgb: Int, dstRgb: Int, srcAlpha: Int, dstAlpha: Int, func: Int) : super(srcRgb, dstRgb, srcAlpha, dstAlpha, func)

        override fun apply() {
            super.apply()
            // MC's enable function is fundamentally broken because it is lazy in that it does not update anything
            // if the previously active blend state matches this one. But that assumes that it is the only method which
            // can modify the global GL state, which is just a horrible assumption and MC itself immediately violates
            // it in RenderLayer.
            // So, to actually get our state applied, we gotta do it ourselves.
            this@BlendState.applyState()
        }
    }
    val mc: BlendMode = if (enabled) {
        if (separate) {
            McBlendState(srcRgb.glId, dstRgb.glId, srcAlpha.glId, dstAlpha.glId, equation.glId)
        } else {
            McBlendState(srcRgb.glId, dstRgb.glId, equation.glId)
        }
    } else {
        McBlendState()
    }

    fun activate() = mc.apply()
    //#else
    //$$ fun activate() = applyState()
    //#endif

    private fun applyState() {
        if (enabled) {
            RenderSystem.enableBlend()
        } else {
            RenderSystem.disableBlend()
        }
        RenderSystem.blendEquation(equation.glId)
        RenderSystem.blendFuncSeparate(srcRgb.glId, dstRgb.glId, srcAlpha.glId, dstAlpha.glId)
    }

    companion object {
        @JvmField
        val DISABLED = BlendState(Equation.ADD, Param.ONE, Param.ZERO, enabled = false)
        @JvmField
        val NORMAL = BlendState(Equation.ADD, Param.SRC_ALPHA, Param.ONE_MINUS_SRC_ALPHA)

        @JvmStatic
        fun active() = BlendState(
            //#if MC>=11500
            Equation.fromGl(GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB)) ?: Equation.ADD,
            //#else
            //$$ Equation.fromGl(GL11.glGetInteger(GL14.GL_BLEND_EQUATION)) ?: Equation.ADD,
            //#endif
            Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB)) ?: Param.ONE,
            Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_DST_RGB)) ?: Param.ZERO,
            Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA)) ?: Param.ONE,
            Param.fromGl(GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA)) ?: Param.ZERO,
            GL11.glGetBoolean(GL11.GL_BLEND),
         )
    }

    enum class Equation(internal val mcStr: String, internal val glId: Int) {
        ADD("add", GL14.GL_FUNC_ADD),
        SUBTRACT("subtract", GL14.GL_FUNC_SUBTRACT),
        REVERSE_SUBTRACT("reverse_subtract", GL14.GL_FUNC_REVERSE_SUBTRACT),
        MIN("min", GL14.GL_MIN),
        MAX("max", GL14.GL_MAX),
        ;

        companion object {
            private val byGlId = values().associateBy { it.glId }
            @JvmStatic
            fun fromGl(glId: Int) = byGlId[glId]
        }
    }

    enum class Param(internal val mcStr: String, internal val glId: Int) {
        ZERO("0", GL11.GL_ZERO),
        ONE("1", GL11.GL_ONE),
        SRC_COLOR("srccolor", GL11.GL_SRC_COLOR),
        ONE_MINUS_SRC_COLOR("1-srccolor", GL11.GL_ONE_MINUS_SRC_COLOR),
        DST_COLOR("dstcolor", GL11.GL_DST_COLOR),
        ONE_MINUS_DST_COLOR("1-dstcolor", GL11.GL_ONE_MINUS_DST_COLOR),
        SRC_ALPHA("srcalpha", GL11.GL_SRC_ALPHA),
        ONE_MINUS_SRC_ALPHA("1-srcalpha", GL11.GL_ONE_MINUS_SRC_ALPHA),
        DST_ALPHA("dstalpha", GL11.GL_DST_ALPHA),
        ONE_MINUS_DST_ALPHA("1-dstalpha", GL11.GL_ONE_MINUS_DST_ALPHA),
        ;

        companion object {
            private val byGlId = values().associateBy { it.glId }
            @JvmStatic
            fun fromGl(glId: Int) = byGlId[glId]
        }
    }
}

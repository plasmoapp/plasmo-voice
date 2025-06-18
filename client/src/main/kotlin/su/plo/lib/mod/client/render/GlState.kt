package su.plo.lib.mod.client.render

//#if MC<12105
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import su.plo.lib.mod.client.compat.vulkan.VulkanCompat

//#if FABRIC
import su.plo.lib.mod.client.compat.vulkan.VulkanGlState
//#endif

data class GlState(
    var depthFunc: Int?,
    var cull: Boolean,
    var blendFunc: List<Int>?,
    var depthMask: Boolean,
) {
    fun javaCopy() = this.copy()

    fun apply(current: GlState) {
        val depthFunc = depthFunc
        if (current.depthFunc != depthFunc) {
            current.depthFunc = depthFunc
            if (depthFunc != null) {
                RenderSystem.enableDepthTest()
                RenderSystem.depthFunc(depthFunc)
            } else {
                RenderSystem.disableDepthTest()
            }
        }

        if (current.cull != cull) {
            current.cull = cull
            if (cull) {
                RenderSystem.enableCull()
            } else {
                RenderSystem.disableCull()
            }
        }

        val blendFunc = blendFunc
        if (current.blendFunc != blendFunc) {
            current.blendFunc = blendFunc
            if (blendFunc != null) {
                RenderSystem.enableBlend()
                RenderSystem.blendFuncSeparate(
                    blendFunc[0],
                    blendFunc[1],
                    blendFunc[2],
                    blendFunc[3],
                )
            } else {
                RenderSystem.disableBlend()
            }
        }

        if (current.depthMask != depthMask) {
            current.depthMask = depthMask
            RenderSystem.depthMask(depthMask)
        }
    }

    companion object {
        @JvmStatic
        fun current(): GlState {
            //#if FABRIC
            if (VulkanCompat.hasVulkan) {
                return VulkanGlState.current()
            }
            //#endif

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
                        GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                        GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),
                        GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),
                    )
                else
                    null

            val depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)

            return GlState(
                depthFunc,
                cull,
                blendFunc,
                depthMask,
            )
        }
    }
}
//#endif

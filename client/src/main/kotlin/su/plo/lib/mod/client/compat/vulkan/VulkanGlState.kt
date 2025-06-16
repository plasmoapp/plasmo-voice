package su.plo.lib.mod.client.compat.vulkan

//#if MC<12105
import net.vulkanmod.vulkan.VRenderSystem
import net.vulkanmod.vulkan.shader.PipelineState
import su.plo.lib.mod.client.render.GlState

object VulkanGlState {
    fun current(): GlState =
        GlState(
            VRenderSystem.depthFun.takeIf { VRenderSystem.depthTest },
            VRenderSystem.cull,
            PipelineState.blendInfo
                .takeIf { it.enabled }
                ?.let {
                    listOf(it.srcRgbFactor, it.srcAlphaFactor, it.dstRgbFactor, it.dstAlphaFactor)
                },
            VRenderSystem.depthMask,
        )
}
//#endif

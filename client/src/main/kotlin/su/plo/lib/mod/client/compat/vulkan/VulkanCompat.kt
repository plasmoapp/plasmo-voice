package su.plo.lib.mod.client.compat.vulkan

object VulkanCompat {
    @JvmStatic
    @get:JvmName("hasVulkan")
    val hasVulkan by lazy {
        try {
            Class.forName("net.vulkanmod.vulkan.Vulkan")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}

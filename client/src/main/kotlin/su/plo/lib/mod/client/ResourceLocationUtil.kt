package su.plo.lib.mod.client

import net.minecraft.resources.ResourceLocation

object ResourceLocationUtil {
    @JvmStatic
    fun tryBuild(namespace: String, location: String): ResourceLocation? =
        //#if MC>11802
        ResourceLocation.tryBuild(namespace, location)
        //#else
        //$$ ResourceLocation(namespace, location)
        //#endif
}

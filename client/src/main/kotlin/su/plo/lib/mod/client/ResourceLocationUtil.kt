package su.plo.lib.mod.client

import net.minecraft.ResourceLocationException
import net.minecraft.resources.ResourceLocation

object ResourceLocationUtil {
    @JvmStatic
    fun tryBuild(
        namespace: String,
        location: String,
    ): ResourceLocation? =
        //#if MC>11802
        ResourceLocation.tryBuild(namespace, location)
        //#else
        //$$ ResourceLocation(namespace, location)
        //#endif

    @JvmStatic
    @Throws(ResourceLocationException::class)
    fun build(
        namespace: String,
        location: String,
    ): ResourceLocation =
    //#if MC>=12100
    //$$ ResourceLocation.fromNamespaceAndPath(namespace, location)
        //#else
        ResourceLocation(namespace, location)
    //#endif

    @JvmStatic
    @Throws(ResourceLocationException::class)
    fun parse(location: String): ResourceLocation =
        //#if MC>=12100
        //$$ ResourceLocation.parse(location)
        //#else
        ResourceLocation(location)
        //#endif

    @JvmStatic
    fun mod(location: String): ResourceLocation = tryBuild("plasmovoice", location)!!
}

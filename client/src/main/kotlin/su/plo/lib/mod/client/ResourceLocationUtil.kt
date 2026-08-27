package su.plo.lib.mod.client

import net.minecraft.ResourceLocationException
import net.minecraft.resources.ResourceLocation

object ResourceLocationUtil {
    @JvmStatic
    fun tryBuild(
        namespace: String,
        location: String,
    ): ResourceLocation? =
        ResourceLocation.tryBuild(namespace, location)

    @JvmStatic
    @Throws(ResourceLocationException::class)
    fun build(
        namespace: String,
        location: String,
    ): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath(namespace, location)

    @JvmStatic
    @Throws(ResourceLocationException::class)
    fun parse(location: String): ResourceLocation =
        ResourceLocation.parse(location)

    @JvmStatic
    fun mod(location: String): ResourceLocation = tryBuild("plasmovoice", location)!!
}

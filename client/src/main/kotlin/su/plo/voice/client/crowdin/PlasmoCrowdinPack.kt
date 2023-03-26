package su.plo.voice.client.crowdin

import com.google.common.collect.ImmutableSet
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
import java.io.File
import java.io.InputStream

//#if MC>=11903
import net.minecraft.server.packs.resources.IoSupplier
//#else
//$$ import java.io.FileInputStream
//$$ import java.util.function.Predicate
//#endif

class PlasmoCrowdinPack(
    private val crowdinFolder: File
) : PackResources {

    override fun close() {}

    //#if MC>=11903
    override fun getRootResource(vararg fileNames: String): IoSupplier<InputStream>? =
        File(crowdinFolder, fileNames[0])
            .takeIf { it.exists() }
            ?.let { IoSupplier.create(it.toPath()) }

    override fun getResource(packType: PackType, resourceLocation: ResourceLocation): IoSupplier<InputStream>? {
        if (resourceLocation.namespace != "plasmovoice") return null
        if (!resourceLocation.path.startsWith("lang/")) return null
        return getRootResource(resourceLocation.path.substringAfter("lang/"))
    }

    override fun listResources(
        packType: PackType,
        namespace: String,
        prefix: String,
        resourceOutput: PackResources.ResourceOutput
    ) {}

    override fun isBuiltin() = true
    //#else
    //$$ override fun getRootResource(fileName: String): InputStream? =
    //$$     File(crowdinFolder, fileName)
    //$$         .takeIf { it.exists() }
    //$$         ?.let { FileInputStream(it) }
    //$$
    //$$ override fun getResource(packType: PackType, resourceLocation: ResourceLocation): InputStream =
    //$$     getRootResource(resourceLocation.path.substringAfter("lang/"))!!
    //$$
    //$$ override fun getResources(
    //$$     packType: PackType,
    //$$     namespace: String,
    //$$     prefix: String,
    //$$     predicate: Predicate<ResourceLocation>
    //$$ ): Collection<ResourceLocation> = emptyList()
    //$$
    //$$ override fun hasResource(packType: PackType, resourceLocation: ResourceLocation): Boolean {
    //$$     if (resourceLocation.namespace != "plasmovoice") return false
    //$$     if (!resourceLocation.path.startsWith("lang/")) return false
    //$$     return File(crowdinFolder, resourceLocation.path.substringAfter("lang/")).exists()
    //$$ }
    //#endif

    override fun getNamespaces(packType: PackType): Set<String> = NAMESPACES

    override fun <T : Any?> getMetadataSection(metadataSectionSerializer: MetadataSectionSerializer<T>) = null

    override fun packId() = "PlasmoCrowdin resource pack"

    companion object {

        private val NAMESPACE = "plasmovoice"
        private val NAMESPACES: Set<String> = ImmutableSet.of(NAMESPACE)
    }
}

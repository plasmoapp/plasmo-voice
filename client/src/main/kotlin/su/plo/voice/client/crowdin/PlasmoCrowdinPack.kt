package su.plo.voice.client.crowdin

import com.google.common.collect.ImmutableSet
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import java.io.File
import java.io.InputStream

import net.minecraft.server.packs.resources.IoSupplier

//#if MC>=12005
//$$ import net.minecraft.server.packs.PackLocationInfo
//$$ import net.minecraft.server.packs.repository.PackSource
//$$ import net.minecraft.network.chat.Component
//$$ import java.util.Optional
//#endif

//#if MC>=12104
//$$ import net.minecraft.server.packs.metadata.MetadataSectionType
//#else
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
//#endif

class PlasmoCrowdinPack(
    private val crowdinFolder: File
) : PackResources {

    override fun close() {}

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

    //#if MC>=12005
    //$$ override fun location(): PackLocationInfo =
    //$$     PackLocationInfo(
    //$$         "plasmovoice_crowdin",
    //$$         Component.literal("Plasmo Voice Crowdin"),
    //$$         PackSource.BUILT_IN,
    //$$         Optional.empty(),
    //$$     )
    //#else
    override fun isBuiltin() = true
    //#endif

    override fun getNamespaces(packType: PackType): Set<String> = NAMESPACES

    //#if MC>=12104
    //$$ override fun <T : Any> getMetadataSection(metadataSectionType: MetadataSectionType<T>): T? = null
    //#else
    override fun <T : Any?> getMetadataSection(metadataSectionSerializer: MetadataSectionSerializer<T>): T? = null
    //#endif

    override fun packId() = "Plasmo Crowdin resource pack"

    companion object {

        private val NAMESPACE = "plasmovoice"
        private val NAMESPACES: Set<String> = ImmutableSet.of(NAMESPACE)
    }
}

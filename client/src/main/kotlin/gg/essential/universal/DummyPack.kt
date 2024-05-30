package gg.essential.universal

//#if MC>=11903
import gg.essential.universal.shader.MCShader
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
import net.minecraft.server.packs.resources.IoSupplier
import java.io.InputStream

//#if MC>=12005
//$$ import net.minecraft.server.packs.PackLocationInfo
//#endif

/**
 * A dummy resource pack for use in [MCShader], since the [Resource] constructor
 * on 1.19.3+ requires a [PackResources] instead of a String name.
 */
object DummyPack : PackResources {

    override fun packId(): String = "__generated__"

    override fun close() {
        throw UnsupportedOperationException()
    }

    override fun getRootResource(vararg strings: String?): IoSupplier<InputStream>? {
        throw UnsupportedOperationException()
    }

    override fun getResource(packType: PackType, resourceLocation: ResourceLocation): IoSupplier<InputStream>? {
        throw UnsupportedOperationException()
    }

    override fun listResources(
        packType: PackType,
        string: String,
        string2: String,
        resourceOutput: PackResources.ResourceOutput
    ) {
        throw UnsupportedOperationException()
    }

    override fun getNamespaces(packType: PackType): MutableSet<String> {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> getMetadataSection(metadataSectionSerializer: MetadataSectionSerializer<T>): T? {
        throw UnsupportedOperationException()
    }

    //#if MC>=12005
    //$$ override fun location(): PackLocationInfo {
    //$$     throw UnsupportedOperationException()
    //$$ }
    //#endif
}
//#endif

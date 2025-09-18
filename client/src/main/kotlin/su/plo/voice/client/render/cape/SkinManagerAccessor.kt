package su.plo.voice.client.render.cape

import java.io.File

//#if MC>=12109
//$$ import net.minecraft.client.renderer.texture.SkinTextureDownloader
//#endif

interface SkinManagerAccessor {

    fun plasmovoice_skinsCacheFolder(): File

    //#if MC>=12109
    //$$ fun plasmovoice_skinTextureDownloader(): SkinTextureDownloader
    //#endif
}

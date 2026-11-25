package su.plo.lib.mod.client.render

import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import su.plo.voice.BaseVoice
import java.util.Base64

//#if MC>=26.4
//$$ import net.minecraft.client.renderer.texture.Base64TextureProvider2d
//#else
import net.minecraft.client.renderer.texture.DynamicTexture
//#endif

private val LOGGER = BaseVoice.createLogger("TextureUtil")

fun registerBase64Texture(
    texture: String,
    textureLocation: ResourceLocation,
): ResourceLocation {
    val base64string = texture.substringAfter("base64;").replace("\n", "")

    // register base64 icon in minecraft resources
    Minecraft.getInstance().execute {
        val image =
            try {
                readBase64Image(base64string)
            } catch (e: Exception) {
                LOGGER.warn("Failed to decode base64 texture {}", textureLocation, e)
                return@execute
            }

        //#if MC>=26.4
        //$$ image.close()
        //$$ Minecraft.getInstance().textureManager.registerProvider(
        //$$     textureLocation,
        //$$     Base64TextureProvider2d(base64string)
        //$$ )
        //#else
        Minecraft.getInstance().textureManager.register(
            textureLocation,
            DynamicTexture(
                //#if MC>=12105
                //$$ { textureLocation.toDebugFileName() },
                //#endif
                image
            )
                //#if MC<12111
                .also { texture ->
                    texture.setFilter(false, true)
                }
                //#endif
        )
        //#endif
    }

    return textureLocation
}

private fun readBase64Image(base64string: String): NativeImage =
    NativeImage.read(Base64.getDecoder().decode(base64string))

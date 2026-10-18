package su.plo.lib.mod.client.render

import com.google.common.base.Charsets
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.util.Base64

//#if MC>=26.4
//$$ import net.minecraft.client.renderer.texture.TextureResources
//#else
import net.minecraft.client.renderer.texture.DynamicTexture
//#endif

fun registerBase64Texture(
    texture: String,
    textureLocation: ResourceLocation,
): ResourceLocation {
    // register base64 icon in minecraft resources
    Minecraft.getInstance().execute {
        //#if MC>=26.4
        //$$ getNativeImageFromBase64(texture).use { image ->
        //$$     Minecraft.getInstance().textureManager.register(
        //$$         textureLocation,
        //$$         TextureResources.from2dImage({ textureLocation.toDebugFileName() }, image)
        //$$     )
        //$$ }
        //#else
        Minecraft.getInstance().textureManager.register(
            textureLocation,
            DynamicTexture(
                //#if MC>=12105
                //$$ { textureLocation.toDebugFileName() },
                //#endif
                getNativeImageFromBase64(texture)
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

fun getNativeImageFromBase64(texture: String): NativeImage {
    val base64string = texture.substringAfter("base64;")
    val base64bytes = Base64.getDecoder().decode(
        base64string.replace("\n".toRegex(), "").toByteArray(Charsets.UTF_8)
    )

    MemoryStack.stackPush().use { memoryStack ->
        val byteBuffer: ByteBuffer = memoryStack.malloc(base64bytes.size)
        byteBuffer.put(base64bytes)
        byteBuffer.rewind()

        return NativeImage.read(byteBuffer)
    }
}

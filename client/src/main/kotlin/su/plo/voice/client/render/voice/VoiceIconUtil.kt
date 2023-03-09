package su.plo.voice.client.render.voice

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation

object VoiceIconUtil {

    fun getIcon(icon: String, iconLocation: ResourceLocation): String {
        if (!icon.startsWith("base64;")) return icon

        // register base64 icon in minecraft resources
        RenderSystem.recordRenderCall {
            Minecraft.getInstance().textureManager.register(
                iconLocation,
                DynamicTexture(NativeImage.fromBase64(icon.substringAfter("base64;"))
            ))
        }

        return iconLocation.toString()
    }
}

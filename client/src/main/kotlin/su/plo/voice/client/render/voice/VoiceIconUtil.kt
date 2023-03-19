package su.plo.voice.client.render.voice

import net.minecraft.resources.ResourceLocation
import su.plo.mod.client.render.registerBase64Texture

object VoiceIconUtil {

    fun getIcon(icon: String, iconLocation: ResourceLocation): String {
        if (!icon.startsWith("base64;")) return icon

        registerBase64Texture(icon, iconLocation)

        return iconLocation.toString()
    }
}

package su.plo.voice.client.render.cape

import com.google.common.hash.Hashing
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.HttpTexture
import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.render.texture.ModPlayerSkins
import su.plo.voice.client.meta.PlasmoVoiceMeta
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DeveloperCapeManager {

    private val loadedCapes: MutableMap<String, ResourceLocation> = ConcurrentHashMap()

    fun clearLoadedCapes() {
        loadedCapes.clear()
    }

    fun registerTextures(playerName: String) {
        if (PlasmoVoiceMeta.META.developers.none { developer ->
                developer.name == playerName || developer.aliases.contains(playerName)
            }) return

        val capeLocation = ResourceLocation("plasmovoice", "developer_capes/${playerName.lowercase()}")

        Util.backgroundExecutor().execute {
            val url = URL("https://vc.plo.su/capes/$playerName.png")

            val texture = MinecraftProfileTexture(url.toString(), HashMap())
            val string = Hashing.sha1().hashUnencodedChars(texture.hash).toString()

            val skinsFolder = (Minecraft.getInstance().skinManager as SkinManagerAccessor).skinsCacheFolder
            val hashFolder = File(skinsFolder, if (string.length > 2) string.substring(0, 2) else "xx")
            val capeFile = File(hashFolder, string)

            if (capeFile.exists() && System.currentTimeMillis() - capeFile.lastModified() > 86_400_000L) {
                capeFile.delete()
            }

            RenderSystem.recordRenderCall {
                Minecraft.getInstance().textureManager.register(
                    capeLocation,
                    HttpTexture(capeFile, texture.url, ModPlayerSkins.getDefaultSkin(UUID.randomUUID()), false) {}
                )
                loadedCapes[playerName] = capeLocation
            }
        }
    }

    fun getCapeLocation(playerName: String): ResourceLocation? =
        loadedCapes[playerName]
}

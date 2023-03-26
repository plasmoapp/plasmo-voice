package su.plo.voice.client.render.cape

import com.google.common.hash.Hashing
import com.google.gson.JsonParser
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.HttpTexture
import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.render.texture.ModPlayerSkins
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DeveloperCapeManager {

    private var developers = setOf(
        "Apehum",
        "KPidS",
        "Venterok",
        "CoolStory_Bob",
        "GNOME__"
    )

    private val loadedCapes: MutableMap<String, ResourceLocation> = ConcurrentHashMap()

    fun clearLoadedCapes() {
        loadedCapes.clear()
    }

    fun fetchDevelopers() {
        val url = URL("https://vc.plo.su/capes/capes.json")

        val developersJson = try {
            JsonParser.parseString(
                url.openStream().bufferedReader().use { it.readText() }
            ).asJsonObject
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        this.developers = developersJson.getAsJsonArray("developers")
            .map { it.asString }
            .toSet()
    }

    fun registerTextures(playerName: String) {
        if (!developers.contains(playerName)) return

        val capeLocation =  ResourceLocation("plasmovoice", "developer_capes/${playerName.lowercase()}")

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

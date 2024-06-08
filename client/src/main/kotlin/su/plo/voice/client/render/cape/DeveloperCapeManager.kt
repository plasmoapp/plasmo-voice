package su.plo.voice.client.render.cape

import com.google.common.base.Suppliers
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.hash.Hashing
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.HttpTexture
import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.ResourceLocationUtil
import su.plo.lib.mod.client.render.texture.ModPlayerSkins
import su.plo.voice.client.meta.PlasmoVoiceMeta
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

//#if MC>=12002
//$$ import net.minecraft.client.resources.PlayerSkin
//#endif

object DeveloperCapeManager {

    private val loadedCapes: Cache<String, Supplier<Supplier<ResourceLocation?>>> = CacheBuilder.newBuilder()
        .expireAfterAccess(10L, TimeUnit.MINUTES)
        .build()

    //#if MC>=12002
    //$$ private val convertedSkins: Cache<String, PlayerSkin> = CacheBuilder.newBuilder()
    //$$     .expireAfterAccess(10L, TimeUnit.MINUTES)
    //$$     .build()
    //$$
    //$$ fun addCapeToSkin(playerName: String, capeTexture: ResourceLocation, skin: PlayerSkin): PlayerSkin {
    //$$     var newSkin = convertedSkins.getIfPresent(playerName)
    //$$
    //$$     if (newSkin == null || skin.texture != newSkin.texture) {
    //$$         newSkin = PlayerSkin(
    //$$             skin.texture(),
    //$$             skin.textureUrl(),
    //$$             capeTexture,
    //$$             capeTexture,
    //$$             skin.model(),
    //$$             skin.secure()
    //$$         )
    //$$         convertedSkins.put(playerName, newSkin)
    //$$     }
    //$$
    //$$     return newSkin
    //$$ }
    //#endif

    fun clearLoadedCapes() {
        loadedCapes.invalidateAll()
        loadedCapes.cleanUp()
    }

    fun hasCape(playerName: String) =
        PlasmoVoiceMeta.META.developers.any { developer ->
            developer.name == playerName || developer.aliases.contains(playerName)
        }

    fun getCapeLocation(playerName: String): ResourceLocation? {
        return loadedCapes.get(playerName) {
            Suppliers.memoize {
                if (!hasCape(playerName))
                    return@memoize Supplier { null }

                val capeLocation = getCapeLocationAsync(playerName)
                Supplier {
                    capeLocation.getNow(null)
                }
            }
        }.get().get()
    }

    private fun getCapeLocationAsync(playerName: String): CompletableFuture<ResourceLocation?> =
        CompletableFuture.supplyAsync {
            val capeLocation = ResourceLocationUtil.tryBuild("plasmovoice", "developer_capes/${playerName.lowercase()}")!!

            val url = URL("https://plasmovoice.com/capes/$playerName.png")

            val texture = MinecraftProfileTexture(url.toString(), HashMap())
            val string = Hashing.sha1().hashUnencodedChars(texture.hash).toString()

            val skinsFolder = (Minecraft.getInstance().skinManager as SkinManagerAccessor).skinsCacheFolder
            val hashFolder = File(skinsFolder, if (string.length > 2) string.substring(0, 2) else "xx")
            val capeFile = File(hashFolder, string)

            if (capeFile.exists() && System.currentTimeMillis() - capeFile.lastModified() > 86_400_000L) {
                capeFile.delete()
            }

            Minecraft.getInstance().textureManager.register(
                capeLocation,
                HttpTexture(capeFile, texture.url, ModPlayerSkins.getDefaultSkin(UUID.randomUUID()), false) {}
            )

            capeLocation
        }
}

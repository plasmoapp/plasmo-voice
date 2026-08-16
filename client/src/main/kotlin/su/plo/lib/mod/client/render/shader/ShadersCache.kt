package su.plo.lib.mod.client.render.shader

import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.ResourceLocationUtil

//#if MC<12105
import com.mojang.blaze3d.vertex.VertexFormat
import gg.essential.universal.shader.BlendState
import gg.essential.universal.shader.UShader
import net.minecraft.client.Minecraft
import su.plo.voice.BaseVoice
import su.plo.voice.client.ModVoiceClient
import java.io.InputStream

//#if MC>=11700
import gg.essential.universal.shader.MCShader
import net.minecraft.client.renderer.ShaderInstance
//#endif

//#endif

object ShadersCache {
    //#if MC<12105
    private val LOGGER = BaseVoice.createLogger("ShadersCache")

    private val shaders: MutableMap<ResourceLocation, Result<UShader>> = HashMap()

    @JvmStatic
    @Synchronized
    fun getOrLoad(
        location: ResourceLocation,
        vertexFormat: VertexFormat,
    //#if MC>=11700
    ): ShaderInstance =
        (load(location, vertexFormat).getOrThrow() as MCShader).mc
    //#else
    //$$ ): UShader =
    //$$     load(location, vertexFormat).getOrThrow()
    //#endif

    @JvmStatic
    @Synchronized
    fun isUsable(
        location: ResourceLocation,
        vertexFormat: VertexFormat,
    ): Boolean = load(location, vertexFormat).isSuccess

    private fun load(
        location: ResourceLocation,
        vertexFormat: VertexFormat,
    ): Result<UShader> =
        shaders.getOrPut(location) {
            runCatching {
                val shader =
                    UShader.fromLegacyShader(
                        readSource(location, "vsh"),
                        readSource(location, "fsh"),
                        BlendState.NORMAL,
                        vertexFormat,
                    )

                check(shader.usable) { "Shader is not usable" }

                shader
            }.onFailure { LOGGER.error("Failed to load shader {}", location, it) }
        }

    private fun readSource(
        location: ResourceLocation,
        extension: String,
    ): String {
        val path = "shaders/${location.path}.$extension"

        return (openFromResourceManager(location.namespace, path) ?: openFromClasspath(location.namespace, path))
            .use { it.readBytes().decodeToString() }
    }

    private fun openFromResourceManager(
        namespace: String,
        path: String,
    ): InputStream? {
        val resourceLocation = ResourceLocationUtil.build(namespace, path)

        val resourceManager = Minecraft.getInstance()?.resourceManager ?: return null

        return runCatching {
            //#if MC>=11900
            resourceManager.getResource(resourceLocation).orElse(null)?.open()
            //#else
            //$$ resourceManager.getResource(resourceLocation)?.inputStream
            //#endif
        }.getOrNull()
    }

    private fun openFromClasspath(
        namespace: String,
        path: String,
    ): InputStream {
        val classpathPath = "assets/$namespace/$path"

        return checkNotNull(ModVoiceClient::class.java.classLoader.getResourceAsStream(classpathPath)) {
            "Shader $classpathPath is missing"
        }
    }
    //#endif
}

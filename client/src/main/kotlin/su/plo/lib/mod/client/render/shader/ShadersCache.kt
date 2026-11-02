package su.plo.lib.mod.client.render.shader

//#if MC<1.21.5
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
//#endif

//#if MC>=1.21.2 && MC<1.21.5
//$$ import net.minecraft.client.Minecraft
//$$ import net.minecraft.client.renderer.ShaderManager
//$$ import net.minecraft.client.renderer.ShaderProgram
//$$ import su.plo.voice.BaseVoice
//#endif

object ShadersCache {
    //#if MC<1.21.5

    //#if MC>=1.21.2 && MC<1.21.5
    //$$ private val logger = BaseVoice.createLogger("ShadersCache")
    //$$ private val shaders: MutableMap<ResourceLocation, ShaderProgram> = HashMap()
    //$$
    //$$ @JvmStatic
    //$$ @Synchronized
    //$$ fun get(location: ResourceLocation): CompiledShaderProgram? {
    //$$     val program = shaders[location] ?: return null
    //$$
    //$$     return try {
    //$$         Minecraft.getInstance().shaderManager.getProgramForLoading(program)
    //$$     } catch (e: ShaderManager.CompilationException) {
    //$$         logger.error("Failed to load shader {}", program.configId(), e)
    //$$         shaders.remove(location)
    //$$         return null
    //$$     }
    //$$ }
    //#else
    private val shaders: MutableMap<ResourceLocation, ShaderInstance> = HashMap()

    @JvmStatic
    @Synchronized
    fun get(location: ResourceLocation): ShaderInstance? = shaders[location]
    //#endif

    @JvmStatic
    @Synchronized
    fun getOrThrow(location: ResourceLocation): ShaderInstance =
        get(location) ?: error("Shader $location not available")

    @JvmStatic
    @Synchronized
    fun isLoaded(location: ResourceLocation): Boolean = shaders.containsKey(location)

    //#if MC>=1.21.2 && MC<1.21.5
    //$$ @JvmStatic
    //$$ @Synchronized
    //$$ fun put(location: ResourceLocation, program: ShaderProgram) {
    //$$     shaders[location] = program
    //$$ }
    //#else
    @JvmStatic
    @Synchronized
    fun put(
        location: ResourceLocation,
        shader: ShaderInstance,
    ) {
        shaders[location] = shader
    }
    //#endif

    //#endif
}

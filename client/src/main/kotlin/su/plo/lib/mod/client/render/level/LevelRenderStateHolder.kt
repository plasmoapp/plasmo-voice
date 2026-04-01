package su.plo.lib.mod.client.render.level

import net.minecraft.resources.ResourceLocation

//#if MC>=1.21.11
//$$ import com.google.common.collect.Maps
//$$ import net.minecraft.client.renderer.state.LevelRenderState
//$$
//#if FABRIC
//$$ import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
//$$
//$$ private val renderStateKeys: MutableMap<Identifier, RenderStateDataKey<Any>> = Maps.newConcurrentMap()
//$$
//$$ class LevelRenderStateHolder(
//$$     val state: LevelRenderState,
//$$ ) {
//$$     fun set(key: Identifier, value: Any) {
//$$         state.setData(
//$$             renderStateKeys.getOrPut(key) { RenderStateDataKey.create() },
//$$             value,
//$$         )
//$$     }
//$$
//$$     @Suppress("UNCHECKED_CAST")
//$$     fun <T> get(key: Identifier): T? =
//$$         state.getData(renderStateKeys.getOrPut(key) { RenderStateDataKey.create() }) as T?
//$$ }
//#elseif NEOFORGE
//$$ import net.minecraft.util.context.ContextKey
//$$
//$$ private val renderStateKeys: MutableMap<Identifier, ContextKey<Any>> = Maps.newConcurrentMap()
//$$
//$$ class LevelRenderStateHolder(
//$$     val state: LevelRenderState,
//$$ ) {
//$$     fun set(key: Identifier, value: Any) {
//$$         state.setRenderData(
//$$             renderStateKeys.getOrPut(key) { ContextKey(key) },
//$$             value,
//$$         )
//$$     }
//$$
//$$     @Suppress("UNCHECKED_CAST")
//$$     fun <T> get(key: Identifier): T? =
//$$         state.getRenderData(renderStateKeys.getOrPut(key) { ContextKey(key) }) as T?
//$$ }
//#endif

//#else
class LevelRenderStateHolder {
    private val states: MutableMap<ResourceLocation, Any> = HashMap()

    fun set(key: ResourceLocation, value: Any) {
        states[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: ResourceLocation): T? =
        states[key] as? T
}
//#endif

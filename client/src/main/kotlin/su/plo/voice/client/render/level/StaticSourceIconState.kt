package su.plo.voice.client.render.level

import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.ResourceLocationUtil

val STATIC_SOURCE_ICON_STATE_KEY = ResourceLocationUtil.mod("render/static_source_icon")

data class StaticSourceIconState(
    val entries: List<Entry>,
) {
    data class Entry(
        val x: Double,
        val y: Double,
        val z: Double,
        val light: Int,
        val iconLocation: ResourceLocation,
    )
}

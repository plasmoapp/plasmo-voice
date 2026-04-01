package su.plo.voice.client.render.level

import su.plo.lib.mod.client.ResourceLocationUtil

val DISTANCE_VISUALIZE_STATE_KEY = ResourceLocationUtil.mod("render/visualize_distance")

data class DistanceVisualizeState(
    val entries: List<Entry>,
) {
    data class Entry(
        val x: Double,
        val y: Double,
        val z: Double,
        val radius: Float,
        val color: Int,
    )
}

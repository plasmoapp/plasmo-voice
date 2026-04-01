package su.plo.voice.client.render.level

import com.google.common.collect.Maps
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.phys.Vec3
import su.plo.lib.mod.client.render.level.LevelRenderStateHolder
import su.plo.slib.api.position.Pos3d
import su.plo.voice.api.client.event.render.VoiceDistanceRenderEvent
import su.plo.voice.api.client.render.DistanceVisualizer
import su.plo.voice.client.BaseVoiceClient
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.client.event.LevelExtractRenderStateEvent
import su.plo.voice.client.extension.position
import su.plo.voice.client.extension.renderDistanceValue
import java.util.UUID
import kotlin.math.max

class DistanceVisualizeStateExtractor(
    private val voiceClient: BaseVoiceClient,
    private val config: VoiceClientConfig,
) : DistanceVisualizer, LevelExtractRenderStateEvent.Callback {
    private val entries: MutableMap<UUID, Entry> = Maps.newConcurrentMap()

    override fun onExtract(
        level: ClientLevel,
        camera: Camera,
        delta: Float,
        state: LevelRenderStateHolder,
    ) {
        val distanceState = extractRenderState(camera, delta)
        state.set(DISTANCE_VISUALIZE_STATE_KEY, distanceState)
    }

    override fun render(radius: Int, color: Int, position: Pos3d?) {
        if (!config.advanced.visualizeVoiceDistance.value()) return
        if (radius < 2 || radius > Minecraft.getInstance().options.renderDistanceValue() * 16) return

        val event = VoiceDistanceRenderEvent(this, radius, color)
        if (!voiceClient.getEventBus().fire<VoiceDistanceRenderEvent?>(event)) return

        val key: UUID?
        if (position == null) {
            key = UUID.fromString("00000000-0000-0000-0000-000000000000")
        } else {
            key = UUID.randomUUID()
        }

        entries[key] = Entry(
            color,
            radius.toFloat(),
            if (position != null) Vec3(position.x, position.y, position.z) else null
        )
    }

    private fun extractRenderState(
        camera: Camera,
        delta: Float,
    ): DistanceVisualizeState {
        val renderDistance = Minecraft.getInstance().options.renderDistanceValue() * 16
        val clientPlayer = Minecraft.getInstance().player ?: return DistanceVisualizeState(emptyList())

        entries
            .filter { (_, entry) ->
                entry.alpha == 0.0f ||
                        !config.advanced.visualizeVoiceDistance.value() ||
                        (entry.position != null && camera.position().distanceTo(entry.position) > renderDistance)
            }
            .forEach { entries.remove(it.key) }

        val list = entries.values.map {
            if (System.currentTimeMillis() - it.createdAt > 2000L) {
                it.alpha = max(0f, it.alpha - (10f * delta))
            }

            val entryPosition = it.position ?: clientPlayer.position()

            val color = ((it.alpha.toInt() and 0xFF) shl 24) or it.color

            DistanceVisualizeState.Entry(
                entryPosition.x,
                entryPosition.y,
                entryPosition.z,
                it.radius,
                color,
            )
        }

        return DistanceVisualizeState(list)
    }

    private class Entry(
        val color: Int = 0,
        val radius: Float = 0f,
        val position: Vec3? = null,
    ) {
        val createdAt = System.currentTimeMillis()

        var alpha = 150f
    }
}

package su.plo.voice.client.event

import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import su.plo.lib.mod.client.render.level.LevelRenderStateHolder
import su.plo.slib.api.event.GlobalEvent

object LevelExtractRenderStateEvent : GlobalEvent<LevelExtractRenderStateEvent.Callback>(
    { callbacks ->
        Callback { level, camera, delta, state ->
            callbacks.forEach { it.onExtract(level, camera, delta, state) }
        }
    }
) {

    fun interface Callback {
        fun onExtract(
            level: ClientLevel,
            camera: Camera,
            delta: Float,
            state: LevelRenderStateHolder,
        )
    }
}

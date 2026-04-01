package su.plo.voice.client.event

import su.plo.lib.mod.client.render.level.LevelRenderContext
import su.plo.slib.api.event.GlobalEvent

object LevelRenderEvent : GlobalEvent<LevelRenderEvent.Callback>(
    { callbacks ->
        Callback { context ->
            callbacks.forEach { it.onRender(context) }
        }
    }
) {

    fun interface Callback {
        fun onRender(context: LevelRenderContext)
    }
}

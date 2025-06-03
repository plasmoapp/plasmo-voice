package su.plo.voice.client.event

import su.plo.lib.mod.client.render.gui.GuiRenderContext
import su.plo.slib.api.event.GlobalEvent
import su.plo.voice.client.event.HudRenderEvent.Callback

object HudRenderEvent : GlobalEvent<Callback>(
    { callbacks ->
        Callback { context, delta ->
            callbacks.forEach { it.onRender(context, delta) }
        }
    }
) {

    fun interface Callback {
        fun onRender(context: GuiRenderContext, delta: Float)
    }
}

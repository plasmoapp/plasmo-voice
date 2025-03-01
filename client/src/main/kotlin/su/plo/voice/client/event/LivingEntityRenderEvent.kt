package su.plo.voice.client.event

import com.mojang.blaze3d.vertex.PoseStack
import su.plo.lib.mod.client.render.entity.LivingEntityRenderState
import su.plo.slib.api.event.GlobalEvent
import su.plo.voice.client.event.LivingEntityRenderEvent.Callback

object LivingEntityRenderEvent : GlobalEvent<Callback>(
    { callbacks ->
        Callback { entity, stack, light ->
            callbacks.forEach { it.onRender(entity, stack, light) }
        }
    }
) {

    fun interface Callback {
        fun onRender(
            entityRenderState: LivingEntityRenderState,
            stack: PoseStack,
            light: Int
        )
    }
}
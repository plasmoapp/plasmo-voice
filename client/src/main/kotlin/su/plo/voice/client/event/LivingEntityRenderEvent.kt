package su.plo.voice.client.event

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.LivingEntity
import su.plo.slib.api.event.GlobalEvent
import su.plo.voice.client.event.LivingEntityRenderEvent.Callback

object LivingEntityRenderEvent : GlobalEvent<Callback>(
    { callbacks ->
        Callback { entity, stack, light, shouldRenderLabel ->
            callbacks.forEach { it.onRender(entity, stack, light, shouldRenderLabel) }
        }
    }
) {

    fun interface Callback {
        fun onRender(
            entity: LivingEntity,
            stack: PoseStack,
            light: Int,
            shouldRenderLabel: Boolean
        )
    }
}
package su.plo.voice.client.event

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.ClientLevel
import su.plo.slib.api.event.GlobalEvent

object LevelRenderEvent : GlobalEvent<LevelRenderEvent.Callback>(
    { callbacks ->
        Callback { level, stack, delta ->
            callbacks.forEach { it.onRender(level, stack, delta) }
        }
    }
) {

    fun interface Callback {
        fun onRender(level: ClientLevel, stack: PoseStack, delta: Float)
    }
}

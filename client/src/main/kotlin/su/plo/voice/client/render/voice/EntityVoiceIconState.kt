package su.plo.voice.client.render.voice

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import su.plo.slib.api.chat.component.McTextComponent

data class EntityVoiceIconState(
    val iconLocation: ResourceLocation?,
    val iconOffset: Vec3,
    val percentText: McTextComponent?,
)

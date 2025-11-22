package su.plo.voice.client.extension

import net.minecraft.client.Camera
import net.minecraft.world.phys.Vec3

fun Camera.position(): Vec3 =
    //#if MC>=12111
    //$$ this.position()
    //#else
    position
    //#endif

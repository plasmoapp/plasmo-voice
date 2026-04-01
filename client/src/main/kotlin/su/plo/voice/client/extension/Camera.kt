package su.plo.voice.client.extension

import net.minecraft.client.Camera
import net.minecraft.world.phys.Vec3

//#if MC>=1.21.11
//$$ import net.minecraft.client.renderer.state.CameraRenderState
//#endif

//#if MC>=1.21.11
//$$ fun CameraRenderState.position(): Vec3 =
//$$     pos
//#endif

fun Camera.position(): Vec3 =
    //#if MC>=12111
    //$$ this.position()
    //#else
    position
    //#endif

package su.plo.lib.mod.client.render.level

import com.mojang.blaze3d.vertex.PoseStack

//#if MC>=1.21.11
//$$ import net.minecraft.client.renderer.state.CameraRenderState
//#else
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
//#endif

//#if MC>=26.2
//$$ import net.minecraft.client.renderer.SubmitNodeCollector
//#endif

data class LevelRenderContext(
    //#if MC<1.21.11
    val level: ClientLevel,
    val camera: Camera,
    //#endif
    val stack: PoseStack,
    val state: LevelRenderStateHolder,
    //#if MC>=26.2
    //$$ val submitNodeCollector: SubmitNodeCollector,
    //#endif
) {
    //#if MC>=1.21.11
    //$$ val camera: CameraRenderState
    //$$     get() = state.state.cameraRenderState
    //#endif
}

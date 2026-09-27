package su.plo.voice.client.extension

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import net.minecraft.world.entity.Display
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import su.plo.voice.client.mixin.accessor.DisplayAccessor

fun Display.transformationOffset(): Vec3? {
    val renderState = renderState() ?: return null
    val translation = renderState.transformation()
        .get(interpolationProgress())
        .translation

    val offset = renderState.orientation(this).transform(translation, Vector3f())
    return Vec3(offset.x.toDouble(), offset.y.toDouble(), offset.z.toDouble())
}

// same as DisplayRenderer#calculateInterpolationProgress, but without lastProgress setter
private fun Display.interpolationProgress(): Float {
    val accessor = this as DisplayAccessor
    val duration = accessor.plasmovoice_getInterpolationDuration()
    if (duration <= 0) return 1f

    val elapsed = (tickCount - accessor.plasmovoice_getInterpolationStartClientTick()).toFloat() + 1f
    return Mth.clamp(Mth.inverseLerp(elapsed, 0f, duration.toFloat()), 0f, 1f)
}

// same as DisplayRenderer#calculateOrientation
private fun Display.RenderState.orientation(display: Display): Quaternionf {
    val camera = Minecraft.getInstance().gameRenderer.mainCamera
    val cameraYRot = camera.yRot - 180f
    val cameraXRot = -camera.xRot

    val (yRot, xRot) = when (billboardConstraints()) {
        Display.BillboardConstraints.FIXED -> display.yRot to display.xRot
        Display.BillboardConstraints.HORIZONTAL -> display.yRot to cameraXRot
        Display.BillboardConstraints.VERTICAL -> cameraYRot to display.xRot
        Display.BillboardConstraints.CENTER -> cameraYRot to cameraXRot
    }

    return Quaternionf().rotationYXZ(-yRot * Mth.DEG_TO_RAD, xRot * Mth.DEG_TO_RAD, 0f)
}

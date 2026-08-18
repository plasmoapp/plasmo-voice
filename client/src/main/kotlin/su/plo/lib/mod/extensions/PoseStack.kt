package su.plo.lib.mod.extensions

import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Quaternionf

@JvmOverloads
fun PoseStack.rotate(angle: Float, x: Float, y: Float, z: Float, degrees: Boolean = true) {
    if (angle == 0f) return

    last().run {
        val angleRadians = if (degrees) Math.toRadians(angle.toDouble()).toFloat() else angle
        mulPose(Quaternionf().rotateAxis(angleRadians, x, y, z))
    }
}

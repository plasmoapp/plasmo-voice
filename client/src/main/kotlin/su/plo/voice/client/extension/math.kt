package su.plo.voice.client.extension

import net.minecraft.world.phys.Vec3
import su.plo.slib.api.position.Pos3d

fun Pos3d.toVec3() = Vec3(this.x, this.y, this.z)

fun Vec3.toFloatArray() = floatArrayOf(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())

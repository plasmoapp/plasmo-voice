package su.plo.voice.client.utils

import net.minecraft.world.phys.Vec3
import su.plo.voice.proto.data.pos.Pos3d

fun Pos3d.toVec3() = Vec3(this.x, this.y, this.z)

fun Vec3.toFloatArray() = floatArrayOf(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())

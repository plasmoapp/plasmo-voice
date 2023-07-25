package su.plo.lib.mod.extensions

import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

fun ofSize(vec3: Vec3, d: Double, e: Double, f: Double): AABB? {
    return AABB(
        vec3.x - d / 2.0,
        vec3.y - e / 2.0,
        vec3.z - f / 2.0,
        vec3.x + d / 2.0,
        vec3.y + e / 2.0,
        vec3.z + f / 2.0
    )
}

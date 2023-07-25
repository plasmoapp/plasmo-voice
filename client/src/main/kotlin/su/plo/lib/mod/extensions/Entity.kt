package su.plo.lib.mod.extensions

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

fun Entity.level() =
    //#if MC>=11802
    this.getLevel()
    //#else
    //$$ this.level
    //#endif

fun Entity.eyePosition() = Vec3(this.x, this.y + this.eyeHeight, this.z)

fun Entity.xRot() = this.xRot

fun Entity.yRot() = this.yRot

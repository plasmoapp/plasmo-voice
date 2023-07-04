package su.plo.lib.mod.server.extensions

import net.minecraft.world.entity.Entity

fun Entity.level() =
    //#if MC>=11802
    this.getLevel()
    //#else
    //$$ this.level
    //#endif

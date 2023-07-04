package su.plo.lib.mod.server.extensions

import net.minecraft.server.level.ServerPlayer

fun ServerPlayer.serverLevel() =
    //#if MC>=12000
    //$$ this.serverLevel()
    //#else
    this.level
    //#endif

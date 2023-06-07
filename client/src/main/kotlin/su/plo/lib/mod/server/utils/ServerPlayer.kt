package su.plo.lib.mod.server.utils

import net.minecraft.server.level.ServerPlayer

fun ServerPlayer.serverLevel() =
    //#if MC>=12000
    //$$ this.serverLevel()
    //#else
    this.level
    //#endif

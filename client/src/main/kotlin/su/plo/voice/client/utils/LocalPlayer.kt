package su.plo.voice.client.utils

import net.minecraft.client.player.LocalPlayer

fun LocalPlayer.level() =
    //#if MC>=12000
    //$$ this.level()
    //#else
    this.level
    //#endif

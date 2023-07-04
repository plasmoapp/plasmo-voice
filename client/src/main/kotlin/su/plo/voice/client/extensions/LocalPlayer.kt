package su.plo.voice.client.extensions

import net.minecraft.client.player.LocalPlayer

fun LocalPlayer.level() =
    //#if MC>=12000
    //$$ this.level()
    //#else
    this.level
    //#endif

package su.plo.voice.client.extension

import net.minecraft.client.Options

fun Options.renderDistanceValue(): Int =
    //#if MC>=11900
    this.renderDistance().get()
    //#else
    //$$ this.renderDistance
    //#endif

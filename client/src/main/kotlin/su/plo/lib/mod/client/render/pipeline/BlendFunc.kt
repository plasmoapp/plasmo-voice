package su.plo.lib.mod.client.render.pipeline

import su.plo.lib.mod.client.render.DestFactor
import su.plo.lib.mod.client.render.SourceFactor

//#if MC>=12105
//$$ import com.mojang.blaze3d.pipeline.BlendFunction
//#endif

data class BlendFunc(
    val sourceColor: SourceFactor,
    val destColor: DestFactor,
    val sourceAlpha: SourceFactor,
    val destAlpha: DestFactor,
) {
    val glList by lazy {
        listOf(
            sourceColor.gl(),
            destColor.gl(),
            sourceAlpha.gl(),
            destAlpha.gl(),
        )
    }

    //#if MC>=12105
    //$$ fun mc(): BlendFunction =
    //$$     BlendFunction(
    //$$         sourceColor.mc(),
    //$$         destColor.mc(),
    //$$         sourceAlpha.mc(),
    //$$         destAlpha.mc(),
    //$$     )
    //#endif

    companion object {
        val TRANSLUCENT = BlendFunc(
            SourceFactor.SRC_ALPHA,
            DestFactor.ONE_MINUS_SRC_ALPHA,
            SourceFactor.ONE,
            DestFactor.ONE_MINUS_SRC_ALPHA
        )
    }
}

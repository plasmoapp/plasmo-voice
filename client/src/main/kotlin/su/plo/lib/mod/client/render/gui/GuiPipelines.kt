package su.plo.lib.mod.client.render.gui

import su.plo.lib.mod.client.ResourceLocationUtil
import su.plo.lib.mod.client.render.shader.SolidColorShader

//#if MC>=1.21.6
//$$ import com.mojang.blaze3d.pipeline.RenderPipeline
//$$ import net.minecraft.client.renderer.RenderPipelines
//$$ import su.plo.voice.client.mixin.accessor.RenderPipelinesAccessor
//#else
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.Util
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import java.util.function.Function
//#endif

//#if MC>=1.21.2 && MC<1.21.6
//$$ import net.minecraft.util.TriState
//#elseif MC<1.21.2
import su.plo.lib.mod.client.render.shader.ShadersCache
//#endif

class GuiColorPipeline(
    //#if MC>=1.21.6
    //$$ val mc: RenderPipeline,
    //#else
    val mc: RenderType,
    //#endif
)

//#if MC>=1.21.2
//$$ class GuiTexturePipeline(
//#if MC>=1.21.6
//$$     val mc: RenderPipeline,
//#else
//$$     val mc: Function<ResourceLocation, RenderType>,
//#endif
//$$ )
//#else
class GuiTexturePipeline(
    /**
     * Vanilla `innerBlit` is used when `null`
     */
    val mc: Function<ResourceLocation, RenderType>?,
    val depthTest: Boolean = true,
)
//#endif

object GuiPipelines {
    private val SOLID_COLOR_LOCATION = ResourceLocationUtil.mod("pipeline/gui_texture_solid_color")

    //#if MC>=1.21.6
    //$$ @JvmField val COLOR = GuiColorPipeline(RenderPipelines.GUI)
    //$$ @JvmField val COLOR_OVERLAY = COLOR
    //$$ @JvmField val TEXTURE = GuiTexturePipeline(RenderPipelines.GUI_TEXTURED)
    //$$ @JvmField val TEXTURE_OVERLAY = TEXTURE
    //$$ @JvmField val TEXTURE_SOLID_COLOR = GuiTexturePipeline(
    //$$     RenderPipeline.builder(RenderPipelinesAccessor.plasmovoice_guiTexturedSnippet())
    //$$         .withLocation(SOLID_COLOR_LOCATION)
    //$$         .withVertexShader(SolidColorShader.LOCATION)
    //$$         .withFragmentShader(SolidColorShader.LOCATION)
    //$$         .build()
    //$$ )
    //#elseif MC>=1.21.2
    //$$ @JvmField val COLOR = GuiColorPipeline(RenderType.gui())
    //$$ @JvmField val COLOR_OVERLAY = GuiColorPipeline(RenderType.guiOverlay())
    //$$ @JvmField val TEXTURE = GuiTexturePipeline(RenderType::guiTextured)
    //$$ @JvmField val TEXTURE_OVERLAY = GuiTexturePipeline(RenderType::guiTexturedOverlay)
    //$$ @JvmField val TEXTURE_SOLID_COLOR = GuiTexturePipeline(Util.memoize(::SolidColorRenderType))
    //#else
    @JvmField val COLOR = GuiColorPipeline(RenderType.gui())
    @JvmField val COLOR_OVERLAY = GuiColorPipeline(RenderType.guiOverlay())
    @JvmField val TEXTURE = GuiTexturePipeline(null)
    @JvmField val TEXTURE_OVERLAY = GuiTexturePipeline(null, depthTest = false)
    @JvmField val TEXTURE_SOLID_COLOR = GuiTexturePipeline(Util.memoize(::SolidColorRenderType))
    //#endif

    //#if MC<1.21.6
    private class SolidColorRenderType private constructor(
        shards: List<RenderStateShard>,
    ) : RenderType(
        SOLID_COLOR_LOCATION.toString(),
        DefaultVertexFormat.POSITION_TEX_COLOR,
        VertexFormat.Mode.QUADS,
        1536,
        false,
        false,
        { shards.forEach { it.setupRenderState() } },
        { shards.forEach { it.clearRenderState() } },
    ) {
        constructor(texture: ResourceLocation) : this(
            listOf(
                //#if MC>=1.21.2
                //$$ ShaderStateShard(SolidColorShader.PROGRAM),
                //$$ TextureStateShard(texture, TriState.FALSE, false),
                //#else
                ShaderStateShard { ShadersCache.getOrThrow(SolidColorShader.LOCATION) },
                TextureStateShard(texture, false, false),
                //#endif
                TRANSLUCENT_TRANSPARENCY,
                LEQUAL_DEPTH_TEST,
            )
        )
    }
    //#endif
}

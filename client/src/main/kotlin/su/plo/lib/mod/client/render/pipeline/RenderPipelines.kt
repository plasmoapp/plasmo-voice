package su.plo.lib.mod.client.render.pipeline

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.RenderType
import su.plo.lib.mod.client.ResourceLocationUtil
import su.plo.lib.mod.client.render.DestFactor
import su.plo.lib.mod.client.render.SourceFactor
import su.plo.lib.mod.client.render.VertexFormatMode
import su.plo.lib.mod.client.render.shader.SolidColorShader

//#if MC>=12105
//$$ import net.minecraft.resources.ResourceLocation
//#else
import com.mojang.blaze3d.vertex.VertexFormatElement
//#endif

object RenderPipelines {
    @JvmField
    val GUI_HIGHLIGHT =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/gui_highlight"),
            DefaultVertexFormat.POSITION,
            VertexFormatMode.QUADS,
        ) // todo: withColorLogic(LogicOp.OR_REVERSE)?

    @JvmField
    val GUI_COLOR =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/gui_color"),
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormatMode.QUADS,
        ) {
            blendFunc = BlendFunc.TRANSLUCENT
        }

    @JvmField
    val GUI_COLOR_OVERLAY =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/gui_color_overlay"),
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormatMode.QUADS,
        ) {
            blendFunc = BlendFunc.TRANSLUCENT
            depthTestFunc = AlphaFunc.ALWAYS
            depthMask = false
        }

    @JvmField
    val GUI_TEXTURE_SOLID_COLOR =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/gui_texture_solid_color"),
            //#if MC>=12105
            //$$ ResourceLocationUtil.mod("position_tex_solid_color"),
            //$$ ResourceLocationUtil.mod("position_tex_solid_color"),
            //#elseif MC>=11700
            {
                val shader = SolidColorShader.getShader() as gg.essential.universal.shader.MCShader
                shader.mc
            },
            //#else
            //$$ SolidColorShader.getShader(),
            //#endif
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormatMode.QUADS,
        ) {
            samplers += "Sampler0"
        }

    @JvmField
    val GUI_TEXTURE =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/gui_texture"),
            DefaultVertexFormat.POSITION_TEX,
            VertexFormatMode.QUADS,
        ) {
            samplers += "Sampler0"
            blendFunc = BlendFunc.TRANSLUCENT
        }

    @JvmField
    val GUI_TEXTURE_OVERLAY =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/gui_texture_overlay"),
            DefaultVertexFormat.POSITION_TEX,
            VertexFormatMode.QUADS,
        ) {
            samplers += "Sampler0"
            blendFunc = BlendFunc.TRANSLUCENT
            depthTestFunc = AlphaFunc.ALWAYS
            depthMask = false
        }

    @JvmField
    val GUI_TEXTURE_COLOR =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/gui_texture_color"),
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormatMode.QUADS,
        ) {
            samplers += "Sampler0"
        }

    @JvmField
    val DISTANCE_SPHERE =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/distance_sphere"),
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormatMode.TRIANGLE_STRIP,
        ) {
            cull = false
            blendFunc =
                BlendFunc(
                    SourceFactor.SRC_ALPHA,
                    DestFactor.ONE_MINUS_SRC_ALPHA,
                    SourceFactor.ONE,
                    DestFactor.ZERO
                )
        }

    @JvmField
    val GUI_PARTICLE_TEXTURE_COLOR =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/gui_particle_texture_color"),
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormatMode.QUADS,
        ) {
            samplers += "Sampler0"
            depthTestFunc = AlphaFunc.ALWAYS
        }

    @JvmField
    val TEXT_BACKGROUND =
        renderPipeline(
            ResourceLocationUtil.mod("pipeline/render_type_text_background"),
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormatMode.QUADS,
        ) {
            blendFunc = BlendFunc.TRANSLUCENT
            depthMask = false
            depthTestFunc = AlphaFunc.ALWAYS
        }

    private val renderTypes: MutableMap<RenderType, RenderPipeline> = HashMap()

    @JvmStatic
    fun fromRenderType(name: String, renderType: RenderType) =
        renderTypes.computeIfAbsent(renderType) {
            renderPipeline(
                ResourceLocationUtil.mod("pipeline/render_type_$name"),
                //#if MC>=12105
                //$$ renderType.renderPipeline.vertexShader,
                //$$ renderType.renderPipeline.fragmentShader,
                //#endif
                renderType.format(),
                VertexFormatMode.from(renderType.mode())
            ) {
                //#if MC>=12105
                //$$ mcRenderPipeline = renderType.renderPipeline
                //#else
                if (renderType.format().elements.any { it.usage == VertexFormatElement.Usage.UV }) {
                    samplers += "Sampler0"
                }
                //#endif
            }
        }

    fun RenderType.toRenderPipeline() =
        fromRenderType(toString(), this)
}

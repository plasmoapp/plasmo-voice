package su.plo.lib.mod.client.render.pipeline

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import su.plo.lib.mod.client.ResourceLocationUtil
import su.plo.lib.mod.client.render.DestFactor
import su.plo.lib.mod.client.render.SourceFactor
import su.plo.lib.mod.client.render.VertexFormatMode
import su.plo.lib.mod.client.render.shader.SolidColorShader

object RenderPipelines {
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
            SolidColorShader.LOCATION,
            @Suppress("DEPRECATION")
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
            @Suppress("DEPRECATION")
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
            oit = true
            cull = false
            blendFunc =
                BlendFunc(
                    SourceFactor.SRC_ALPHA,
                    DestFactor.ONE_MINUS_SRC_ALPHA,
                    SourceFactor.ONE,
                    DestFactor.ZERO
                )
        }
}

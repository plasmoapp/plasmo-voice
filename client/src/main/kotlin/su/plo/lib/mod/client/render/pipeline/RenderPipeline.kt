package su.plo.lib.mod.client.render.pipeline

import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.render.VertexFormatMode

//#if MC<12105

import su.plo.lib.mod.client.render.GlState

//#if MC>=12102
//$$ import net.minecraft.client.Minecraft
//$$ import net.minecraft.client.renderer.CoreShaders
//$$ import net.minecraft.client.renderer.ShaderProgram
//#else
import net.minecraft.client.renderer.GameRenderer
//#endif

//#endif

//#if MC>=12105
//$$ import com.mojang.blaze3d.shaders.UniformType
//$$ import com.mojang.blaze3d.vertex.DefaultVertexFormat
//#elseif MC>=11700
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.ShaderInstance
//#else
//$$ import gg.essential.universal.shader.UShader
//#endif

//#if MC>=12111
//$$ import net.minecraft.client.renderer.rendertype.RenderSetup
//$$ import su.plo.voice.client.mixin.accessor.RenderTypeAccessor
//#endif

//#if MC>=12105
//$$ private fun defaultShader(vertexFormat: VertexFormat): ResourceLocation? =
//$$     when (vertexFormat) {
//$$         DefaultVertexFormat.POSITION -> ResourceLocation.withDefaultNamespace("core/position")
//$$         DefaultVertexFormat.POSITION_COLOR -> ResourceLocation.withDefaultNamespace("core/position_color")
//$$         DefaultVertexFormat.POSITION_TEX -> ResourceLocation.withDefaultNamespace("core/position_tex")
//$$         DefaultVertexFormat.POSITION_TEX_COLOR -> ResourceLocation.withDefaultNamespace("core/position_tex_color")
//$$         DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP -> ResourceLocation.withDefaultNamespace("core/position_color_tex_lightmap")
//$$         DefaultVertexFormat.POSITION_COLOR_LIGHTMAP -> ResourceLocation.withDefaultNamespace("core/position_color_lightmap")
//$$         else -> null
//$$     }
//#elseif MC>=12102
//$$ private fun defaultShader(vertexFormat: VertexFormat): (() -> CompiledShaderProgram)? =
//$$     with(Minecraft.getInstance().shaderManager) {
//$$         when (vertexFormat) {
//$$             DefaultVertexFormat.POSITION -> ({ getProgram(CoreShaders.POSITION)!! })
//$$             DefaultVertexFormat.POSITION_COLOR -> ({ getProgram(CoreShaders.POSITION_COLOR)!! })
//$$             DefaultVertexFormat.POSITION_TEX -> ({ getProgram(CoreShaders.POSITION_TEX)!! })
//$$             DefaultVertexFormat.POSITION_TEX_COLOR -> ({ getProgram(CoreShaders.POSITION_TEX_COLOR)!! })
//$$             DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP -> ({ getProgram(CoreShaders.POSITION_COLOR_TEX_LIGHTMAP)!! })
//$$             DefaultVertexFormat.POSITION_COLOR_LIGHTMAP -> ({ getProgram(CoreShaders.POSITION_COLOR_LIGHTMAP)!! })
//$$             else -> null
//$$         }
//$$     }
//#elseif MC>=11700
private fun defaultShader(vertexFormat: VertexFormat): (() -> ShaderInstance)? =
    when (vertexFormat) {
        DefaultVertexFormat.POSITION -> ({ GameRenderer.getPositionShader()!! })
        DefaultVertexFormat.POSITION_COLOR -> ({ GameRenderer.getPositionColorShader()!! })
        DefaultVertexFormat.POSITION_TEX -> ({ GameRenderer.getPositionTexShader()!! })
        DefaultVertexFormat.POSITION_TEX_COLOR -> ({ GameRenderer.getPositionTexColorShader()!! })
        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP -> ({ GameRenderer.getPositionColorTexLightmapShader()!! })
        DefaultVertexFormat.POSITION_COLOR_LIGHTMAP -> ({ GameRenderer.getPositionColorLightmapShader()!! })
        else -> null
    }
//#endif

fun renderPipeline(
    location: ResourceLocation,
    vertexFormat: VertexFormat,
    vertexFormatMode: VertexFormatMode,
    builder: RenderPipeline.Builder.() -> Unit = {},
): RenderPipeline {
    //#if MC>=11700
    val shader = defaultShader(vertexFormat)
        ?: throw IllegalArgumentException("Shader not found")
    //#else
    //$$ val shader: UShader? = null
    //#endif

    return RenderPipeline
        .Builder(
            location,
            //#if MC>=12105
            //$$ shader,
            //$$ shader,
            //#else
            shader,
            //#endif
            vertexFormat,
            vertexFormatMode,
        )
        .apply(builder)
        .build()
}

fun renderPipeline(
    location: ResourceLocation,
    //#if MC>=12105
    //$$ vertexShader: ResourceLocation,
    //$$ fragmentShader: ResourceLocation,
    //#elseif MC>=11700
    shader: () -> ShaderInstance,
    //#else
    //$$ shader: UShader?,
    //#endif
    vertexFormat: VertexFormat,
    vertexFormatMode: VertexFormatMode,
    builder: RenderPipeline.Builder.() -> Unit = {},
) = RenderPipeline
    .Builder(
        location,
        //#if MC>=12105
        //$$ vertexShader,
        //$$ fragmentShader,
        //#else
        shader,
        //#endif
        vertexFormat,
        vertexFormatMode,
    )
    .apply(builder)
    .build()

data class RenderPipeline(
    val location: ResourceLocation,
    //#if MC>=12105
    //$$ val vertexShader: ResourceLocation,
    //$$ val fragmentShader: ResourceLocation,
    //$$ val mcRenderPipeline: com.mojang.blaze3d.pipeline.RenderPipeline,
    //#elseif MC>=11700
    val shader: () -> ShaderInstance,
    //#else
    //$$ val shader: UShader?,
    //#endif
    //#if MC>=12105
    //$$ val mcRenderType: RenderType,
    //#else
    val mcRenderType: RenderType?,
    //#endif
    val samplers: Set<String>,
    private val vertexFormatInner: VertexFormat,
    val vertexFormatMode: VertexFormatMode,
    val depthTestFunc: AlphaFunc,
    val blendFunc: BlendFunc?,
    val cull: Boolean,
    val depthMask: Boolean,
) {
    val glState by lazy {
        //#if MC<12105
        GlState(
            depthTestFunc.takeIf { it != AlphaFunc.ALWAYS }?.gl(),
            cull,
            blendFunc?.glList,
            depthMask,
        )
        //#endif
    }

    val vertexFormat: VertexFormat
        //#if MC>=12105
        //$$ get() = mcRenderType.format()
        //#else
        get() = mcRenderType?.format() ?: vertexFormatInner
        //#endif

    class Builder(
        val location: ResourceLocation,
        //#if MC>=12105
        //$$ val vertexShader: ResourceLocation,
        //$$ val fragmentShader: ResourceLocation,
        //#elseif MC>=11700
        val shader: () -> ShaderInstance,
        //#else
        //$$ val shader: UShader?,
        //#endif
        var vertexFormat: VertexFormat,
        var vertexFormatMode: VertexFormatMode,
    ) {
        var depthTestFunc: AlphaFunc = AlphaFunc.LEQUAL
        var blendFunc: BlendFunc? = null
        var cull: Boolean = true
        var depthMask: Boolean = true
        val samplers: MutableSet<String> = mutableSetOf()

        //#if MC>=12105
        //$$ var mcRenderPipeline: com.mojang.blaze3d.pipeline.RenderPipeline? = null
        //#endif
        var mcRenderType: RenderType? = null

        fun build(): RenderPipeline {
            //#if MC>=12105
            //$$ val pipeline = mcRenderPipeline ?:
            //$$ com.mojang.blaze3d.pipeline.RenderPipeline.builder()
            //$$     // uhhh
            //$$     // it's not correct and depends on the shader,
            //$$     // but it should be fine Clueless
            //#if MC>=12106
            //$$     .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            //$$     .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            //$$     .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            //$$     .withUniform("Globals", UniformType.UNIFORM_BUFFER)
            //#else
            //$$     .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            //$$     .withUniform("ProjMat", UniformType.MATRIX4X4)
            //$$     .withUniform("FogStart", UniformType.FLOAT)
            //$$     .withUniform("FogEnd", UniformType.FLOAT)
            //$$     .withUniform("FogShape", UniformType.INT)
            //$$     .withUniform("FogColor", UniformType.VEC4)
            //$$     .withUniform("ColorModulator", UniformType.VEC4)
            //#endif
            //$$
            //$$      .apply {
            //$$          samplers.forEach(::withSampler)
            //$$      }
            //$$
            //$$     .withLocation(location)
            //$$     .withVertexShader(vertexShader)
            //$$     .withFragmentShader(fragmentShader)
            //$$     .withVertexFormat(vertexFormat, vertexFormatMode.toMc())
            //$$
            //$$     .withDepthTestFunction(depthTestFunc.mcDepthFunc())
            //$$
            //$$     .apply {
            //$$         if (blendFunc != null) {
            //$$             withBlend(blendFunc!!.mc())
            //$$         } else {
            //$$             withoutBlend()
            //$$         }
            //$$     }
            //$$
            //$$     .withCull(cull)
            //$$     .withDepthWrite(depthMask)
            //$$
            //$$     .build()
            //$$
            //$$ val renderType = mcRenderType ?:
            //#if MC>=12111
            //$$     RenderTypeAccessor.plasmovoice_create(
            //$$         location.toString(),
            //$$         RenderSetup.builder(pipeline).createRenderSetup()
            //$$     )
            //#else
            //$$     RenderType.create(
            //$$         location.toString(),
            //$$         1536,
            //$$         pipeline,
            //$$         RenderType.CompositeState.builder().createCompositeState(false)
            //$$     )
            //#endif

            //#else
            val renderType = mcRenderType
            //#endif

            return RenderPipeline(
                location,
                //#if MC>=12105
                //$$ vertexShader,
                //$$ fragmentShader,
                //$$ pipeline,
                //#else
                shader,
                //#endif
                renderType,
                samplers,
                vertexFormat,
                vertexFormatMode,
                depthTestFunc,
                blendFunc,
                cull,
                depthMask,
            )
        }
    }
}

package gg.essential.universal.shader

//#if MC<12105
import com.mojang.blaze3d.vertex.VertexFormat

//#if MC>=11700
import net.minecraft.client.renderer.ShaderInstance
//#endif

interface UShader {
    val usable: Boolean

    fun bind()
    fun unbind()

    companion object {
        @Deprecated(
            "Use the overload which takes a vertex format to ensure proper operation on all versions.",
            replaceWith = ReplaceWith("UShader.fromLegacyShader(vertSource, fragSource, blendState, vertexFormat)")
        )
        fun fromLegacyShader(vertSource: String, fragSource: String, blendState: BlendState): UShader {
            //#if MC>=11700
            return MCShader.fromLegacyShader(vertSource, fragSource, blendState, null)
            //#else
            //$$ return GlShader(vertSource, fragSource, blendState)
            //#endif
        }

        fun fromLegacyShader(vertSource: String, fragSource: String, blendState: BlendState, vertexFormat: VertexFormat): UShader {
            //#if MC>=11700
            return MCShader.fromLegacyShader(vertSource, fragSource, blendState, vertexFormat)
            //#else
            //$$ return GlShader(vertSource, fragSource, blendState)
            //#endif
        }

        //#if MC>=11700
        fun fromMcShader(shader: ShaderInstance, blendState: BlendState): UShader {
            return MCShader(shader)
        }
        //#endif
    }
}
//#endif

package gg.essential.universal.shader

//#if MC<12105
import com.mojang.blaze3d.vertex.VertexFormat

import net.minecraft.client.renderer.ShaderInstance

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
            return MCShader.fromLegacyShader(vertSource, fragSource, blendState, null)
        }

        fun fromLegacyShader(vertSource: String, fragSource: String, blendState: BlendState, vertexFormat: VertexFormat): UShader {
            return MCShader.fromLegacyShader(vertSource, fragSource, blendState, vertexFormat)
        }

        fun fromMcShader(shader: ShaderInstance, blendState: BlendState): UShader {
            return MCShader(shader)
        }
    }
}
//#endif

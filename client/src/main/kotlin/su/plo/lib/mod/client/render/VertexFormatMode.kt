package su.plo.lib.mod.client.render

import com.mojang.blaze3d.vertex.VertexFormat
import org.lwjgl.opengl.GL11

//#if MC>=26.2
//$$ typealias McVertexFormatMode = com.mojang.blaze3d.PrimitiveTopology
//#elseif MC>=11700
typealias McVertexFormatMode = VertexFormat.Mode
//#endif

enum class VertexFormatMode(
    val glMode: Int
) {
    LINES(GL11.GL_LINES),
    LINE_STRIP(GL11.GL_LINE_STRIP),
    TRIANGLES(GL11.GL_TRIANGLES),
    TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
    TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN),
    QUADS(GL11.GL_QUADS);

    //#if MC>=11700
    fun toMc(): McVertexFormatMode =
        when (this) {
            LINES -> McVertexFormatMode.DEBUG_LINES
            LINE_STRIP -> McVertexFormatMode.DEBUG_LINE_STRIP
            TRIANGLES -> McVertexFormatMode.TRIANGLES
            TRIANGLE_STRIP -> McVertexFormatMode.TRIANGLE_STRIP
            TRIANGLE_FAN -> McVertexFormatMode.TRIANGLE_FAN
            QUADS -> McVertexFormatMode.QUADS
        }
    //#endif

    companion object {
        fun from(glMode: Int) =
            when (glMode) {
                GL11.GL_LINES -> LINES
                GL11.GL_LINE_STRIP -> LINE_STRIP
                GL11.GL_TRIANGLES -> TRIANGLES
                GL11.GL_TRIANGLE_STRIP -> TRIANGLE_STRIP
                GL11.GL_TRIANGLE_FAN -> TRIANGLE_FAN
                GL11.GL_QUADS -> QUADS
                else -> throw IllegalArgumentException("Unsupported gl mode $glMode")
            }

        //#if MC>=11700
        fun from(mode: McVertexFormatMode) =
            when (mode) {
                McVertexFormatMode.DEBUG_LINES -> LINE_STRIP
                McVertexFormatMode.DEBUG_LINE_STRIP -> LINE_STRIP
                McVertexFormatMode.TRIANGLES -> TRIANGLES
                McVertexFormatMode.TRIANGLE_STRIP -> TRIANGLE_STRIP
                McVertexFormatMode.TRIANGLE_FAN -> TRIANGLE_FAN
                McVertexFormatMode.QUADS -> QUADS
                else -> throw IllegalArgumentException("Vertex format not supported")
            }
        //#endif
    }
}

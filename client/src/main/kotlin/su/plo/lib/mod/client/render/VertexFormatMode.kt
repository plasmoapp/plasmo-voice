package su.plo.lib.mod.client.render

import com.mojang.blaze3d.vertex.VertexFormat
import org.lwjgl.opengl.GL11

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
    fun toMc(): VertexFormat.Mode =
        when (this) {
            LINES -> VertexFormat.Mode.DEBUG_LINES
            LINE_STRIP -> VertexFormat.Mode.DEBUG_LINE_STRIP
            TRIANGLES -> VertexFormat.Mode.TRIANGLES
            TRIANGLE_STRIP -> VertexFormat.Mode.TRIANGLE_STRIP
            TRIANGLE_FAN -> VertexFormat.Mode.TRIANGLE_FAN
            QUADS -> VertexFormat.Mode.QUADS
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
        fun from(mode: VertexFormat.Mode) =
            when (mode) {
                VertexFormat.Mode.DEBUG_LINES -> LINE_STRIP
                VertexFormat.Mode.DEBUG_LINE_STRIP -> LINE_STRIP
                VertexFormat.Mode.TRIANGLES -> TRIANGLES
                VertexFormat.Mode.TRIANGLE_STRIP -> TRIANGLE_STRIP
                VertexFormat.Mode.TRIANGLE_FAN -> TRIANGLE_FAN
                VertexFormat.Mode.QUADS -> QUADS
                else -> throw IllegalArgumentException("Vertex format not supported")
            }
        //#endif
    }
}

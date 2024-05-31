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
            LINES -> VertexFormat.Mode.LINE_STRIP
            LINE_STRIP -> VertexFormat.Mode.LINE_STRIP
            TRIANGLES -> VertexFormat.Mode.TRIANGLES
            TRIANGLE_STRIP -> VertexFormat.Mode.TRIANGLE_STRIP
            TRIANGLE_FAN -> VertexFormat.Mode.TRIANGLE_FAN
            QUADS -> VertexFormat.Mode.QUADS
        }

    companion object {

        fun fromMc(mode: VertexFormat.Mode) =
            when (mode) {
                VertexFormat.Mode.LINES -> LINE_STRIP
                VertexFormat.Mode.LINE_STRIP -> LINE_STRIP
                VertexFormat.Mode.TRIANGLES -> TRIANGLES
                VertexFormat.Mode.TRIANGLE_STRIP -> TRIANGLE_STRIP
                VertexFormat.Mode.TRIANGLE_FAN -> TRIANGLE_FAN
                VertexFormat.Mode.QUADS -> QUADS
                else -> throw IllegalArgumentException("Vertex format not supported")
            }
    }

    //#endif
}

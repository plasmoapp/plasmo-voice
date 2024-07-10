package su.plo.lib.mod.client.render

import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.PoseStack

class VertexBuilder private constructor(private val buffer: BufferBuilder) {

    fun position(stack: PoseStack, x: Float, y: Float, z: Float) = apply {
        //#if MC>=12100
        //$$ buffer.addVertex(stack.last().pose(), x, y, z)
        //#else
        buffer.vertex(stack.last().pose(), x, y, z)
        //#endif
    }

    fun uv(u: Float, v: Float) = apply {
        //#if MC>=12100
        //$$ buffer.setUv(u, v)
        //#else
        buffer.uv(u, v)
        //#endif
    }

    fun overlay(u: Int) = apply {
        //#if MC>=12100
        //$$ buffer.setOverlay(u)
        //#else
        buffer.overlayCoords(u)
        //#endif
    }

    fun light(u: Int) = apply {
        //#if MC>=12100
        //$$ buffer.setLight(u)
        //#else
        buffer.uv2(u)
        //#endif
    }

    fun light(u: Int, v: Int) = apply {
        //#if MC>=12100
        //$$ buffer.setUv2(u, v)
        //#else
        buffer.uv2(u, v)
        //#endif
    }

    fun color(r: Int, g: Int, b: Int, a: Int) = apply {
        //#if MC>=12100
        //$$ buffer.setColor(r, g, b, a)
        //#else
        buffer.color(r, g, b, a)
        //#endif
    }

    fun color(r: Float, g: Float, b: Float, a: Float) = apply { 
        //#if MC>=12100
        //$$ buffer.setColor(r, g, b, a)
        //#else
        buffer.color(r, g, b, a)
        //#endif
    }

    fun normal(stack: PoseStack, x: Float, y: Float, z: Float) = apply {
        //#if MC>=12100
        //$$ buffer.setNormal(stack.last(), x, y, z)
        //#elseif MC>=12005
        //$$ buffer.normal(stack.last(), x, y, z)
        //#else
        buffer.normal(stack.last().normal(), x, y, z)
        //#endif
    }

    fun end() = apply { 
        //#if MC<12100
        buffer.endVertex()
        //#endif
    }
    
    companion object {
        @JvmStatic
        fun create(buffer: BufferBuilder): VertexBuilder =
            VertexBuilder(buffer)
    }

}

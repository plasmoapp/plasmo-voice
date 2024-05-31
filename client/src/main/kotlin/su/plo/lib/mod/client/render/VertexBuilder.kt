package su.plo.lib.mod.client.render

import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.PoseStack

class VertexBuilder private constructor(private val buffer: BufferBuilder) {

    fun position(stack: PoseStack, x: Float, y: Float, z: Float) = apply {
        buffer.vertex(stack.last().pose(), x, y, z)
    }

    fun uv(u: Float, v: Float) = apply {
        buffer.uv(u, v)
    }

    fun overlay(u: Int) = apply {
        buffer.overlayCoords(u)
    }

    fun light(u: Int) = apply {
        //#if MC>=12100
        //$$ buffer.setLight(u)
        //#else
        buffer.uv2(u)
        //#endif
    }

    fun light(u: Int, v: Int) = apply {
        buffer.uv2(u, v)
    }

    fun color(r: Int, g: Int, b: Int, a: Int) = apply {
        buffer.color(r, g, b, a)
    }

    fun color(r: Float, g: Float, b: Float, a: Float) = apply { 
        buffer.color(r, g, b, a)
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

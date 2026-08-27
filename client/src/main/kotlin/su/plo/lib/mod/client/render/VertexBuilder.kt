package su.plo.lib.mod.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer

class VertexBuilder private constructor(private val buffer: VertexConsumer) {

    fun position(stack: PoseStack, x: Float, y: Float, z: Float) = apply {
        buffer.addVertex(stack.last().pose(), x, y, z)
    }

    fun position(pose: PoseStack.Pose, x: Float, y: Float, z: Float) = apply {
        buffer.addVertex(pose, x, y, z)
    }

    fun uv(u: Float, v: Float) = apply {
        buffer.setUv(u, v)
    }

    fun overlay(u: Int) = apply {
        buffer.setOverlay(u)
    }

    fun light(u: Int) = apply {
        buffer.setLight(u)
    }

    fun light(u: Int, v: Int) = apply {
        buffer.setUv2(u, v)
    }

    fun color(r: Int, g: Int, b: Int, a: Int) = apply {
        buffer.setColor(r, g, b, a)
    }

    fun color(r: Float, g: Float, b: Float, a: Float) = apply { 
        buffer.setColor(r, g, b, a)
    }

    fun normal(stack: PoseStack, x: Float, y: Float, z: Float) = apply {
        buffer.setNormal(stack.last(), x, y, z)
    }

    fun normal(pose: PoseStack.Pose, x: Float, y: Float, z: Float) = apply {
        buffer.setNormal(pose, x, y, z)
    }

    fun end() = apply { 
    }
    
    companion object {
        @JvmStatic
        fun create(buffer: VertexConsumer): VertexBuilder =
            VertexBuilder(buffer)
    }
}

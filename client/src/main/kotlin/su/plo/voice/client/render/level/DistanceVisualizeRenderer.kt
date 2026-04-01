package su.plo.voice.client.render.level

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.VertexBuilder.Companion.create
import su.plo.lib.mod.client.render.level.LevelRenderContext
import su.plo.lib.mod.client.render.pipeline.RenderPipelines
import su.plo.voice.client.event.LevelRenderEvent
import su.plo.voice.client.extension.position
import kotlin.math.cos
import kotlin.math.sin

private const val SPHERE_STACK = 18
private const val SPHERE_SLICE = 36

class DistanceVisualizeRenderer : LevelRenderEvent.Callback {

    override fun onRender(context: LevelRenderContext) {
        val entries = context.state.get<DistanceVisualizeState>(DISTANCE_VISUALIZE_STATE_KEY)?.entries
            ?: return

        entries.forEach { entry ->
            renderEntry(context, entry)
        }
    }

    private fun renderEntry(
        context: LevelRenderContext,
        entry: DistanceVisualizeState.Entry,
    ) {
        val stack = context.stack
        val cameraPosition = context.camera.position()

        stack.pushPose()

        //#if MC<11800
        //$$ stack.last().pose().setIdentity()
        //$$ stack.last().normal().setIdentity()
        //#endif

        stack.translate(
            entry.x - cameraPosition.x,
            entry.y - cameraPosition.y,
            entry.z - cameraPosition.z,
        )

        val buffer = RenderUtil.beginBuffer(RenderPipelines.DISTANCE_SPHERE)
        buildVertices(entry, stack.last(), buffer)
        RenderUtil.drawBuffer(buffer, RenderPipelines.DISTANCE_SPHERE)

        stack.popPose()
    }

    private fun buildVertices(
        entry: DistanceVisualizeState.Entry,
        stack: PoseStack.Pose,
        buffer: VertexConsumer
    ) {
        val alpha = (entry.color shr 24) and 0xff
        val red = (entry.color shr 16) and 0xFF
        val green = (entry.color shr 8) and 0xFF
        val blue = entry.color and 0xFF

        val stackStep = (Math.PI / SPHERE_STACK).toFloat()
        val sliceStep = (Math.PI / SPHERE_SLICE).toFloat()

        var r0: Float
        var r1: Float
        var alpha0: Float
        var alpha1: Float
        var x0: Float
        var x1: Float
        var y0: Float
        var y1: Float
        var z0: Float
        var z1: Float
        var beta: Float

        for (i in 0..<SPHERE_STACK) {
            alpha0 = (-Math.PI / 2 + i * stackStep).toFloat()
            alpha1 = alpha0 + stackStep
            r0 = (entry.radius * cos(alpha0.toDouble())).toFloat()
            r1 = (entry.radius * cos(alpha1.toDouble())).toFloat()

            y0 = (entry.radius * sin(alpha0.toDouble())).toFloat()
            y1 = (entry.radius * sin(alpha1.toDouble())).toFloat()

            for (j in 0..<(SPHERE_SLICE shl 1)) {
                beta = j * sliceStep
                x0 = (r0 * cos(beta.toDouble())).toFloat()
                x1 = (r1 * cos(beta.toDouble())).toFloat()

                z0 = (-r0 * sin(beta.toDouble())).toFloat()
                z1 = (-r1 * sin(beta.toDouble())).toFloat()

                create(buffer)
                    .position(stack, x0, y0, z0)
                    .color(red, green, blue, alpha)
                    .end()
                create(buffer)
                    .position(stack, x1, y1, z1)
                    .color(red, green, blue, alpha)
                    .end()
            }
        }
    }
}

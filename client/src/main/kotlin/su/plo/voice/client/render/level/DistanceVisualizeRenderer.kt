package su.plo.voice.client.render.level

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import su.plo.lib.mod.client.render.VertexBuilder.Companion.create
import su.plo.lib.mod.client.render.level.LevelRenderContext
import su.plo.voice.client.event.LevelRenderEvent
import su.plo.voice.client.extension.position
import kotlin.math.cos
import kotlin.math.sin

//#if MC>=1.21.11
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes
//#else
import net.minecraft.client.renderer.RenderType
//#endif

private const val SPHERE_STACK = 18
private const val SPHERE_SLICE = 72

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

        stack.translate(
            entry.x - cameraPosition.x,
            entry.y - cameraPosition.y,
            entry.z - cameraPosition.z,
        )

        //#if MC>=1.21.11
        //$$ val renderType = RenderTypes.debugQuads()
        //#else
        val renderType = RenderType.debugQuads()
        //#endif

        context.submitGeometry(renderType) { pose, buffer ->
            buildVertices(entry, pose, buffer)
        }

        stack.popPose()
    }

    private fun buildVertices(
        entry: DistanceVisualizeState.Entry,
        pose: PoseStack.Pose,
        buffer: VertexConsumer,
    ) {
        val alpha = (entry.color shr 24) and 0xff
        val red = (entry.color shr 16) and 0xFF
        val green = (entry.color shr 8) and 0xFF
        val blue = entry.color and 0xFF

        val stackStep = Math.PI / SPHERE_STACK
        val sliceStep = 2 * Math.PI / SPHERE_SLICE

        fun vertex(radius: Float, y: Float, slice: Int) {
            val beta = slice * sliceStep
            create(buffer)
                .position(pose, radius * cos(beta).toFloat(), y, -radius * sin(beta).toFloat())
                .color(red, green, blue, alpha)
                .end()
        }

        for (i in 0..<SPHERE_STACK) {
            val alpha0 = -Math.PI / 2 + i * stackStep
            val alpha1 = alpha0 + stackStep
            val r0 = (entry.radius * cos(alpha0)).toFloat()
            val r1 = (entry.radius * cos(alpha1)).toFloat()
            val y0 = (entry.radius * sin(alpha0)).toFloat()
            val y1 = (entry.radius * sin(alpha1)).toFloat()

            for (j in 0..<SPHERE_SLICE) {
                vertex(r0, y0, j)
                vertex(r1, y1, j)
                vertex(r1, y1, j + 1)
                vertex(r0, y0, j + 1)
            }
        }
    }
}

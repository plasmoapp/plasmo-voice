package su.plo.voice.client.render.level

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import su.plo.lib.mod.client.render.VertexBuilder.Companion.create
import su.plo.lib.mod.client.render.level.LevelRenderContext
import su.plo.lib.mod.client.render.submitTexturedGeometry
import su.plo.voice.client.event.LevelRenderEvent
import su.plo.voice.client.extension.position

//#if MC<1.21.11
import su.plo.lib.mod.extensions.rotate
//#endif

class StaticSourceIconRenderer : LevelRenderEvent.Callback {

    override fun onRender(context: LevelRenderContext) {
        val entries = context.state.get<StaticSourceIconState>(STATIC_SOURCE_ICON_STATE_KEY)?.entries
            ?: return

        entries.forEach { entry ->
            renderEntry(context, entry)
        }
    }

    private fun renderEntry(
        context: LevelRenderContext,
        entry: StaticSourceIconState.Entry,
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
        //$$ stack.mulPose(context.camera.orientation)
        //$$ stack.scale(0.025f, -0.025f, 0.025f)
        //#else
        stack.rotate(-context.camera.yRot, 0f, 1f, 0f)
        stack.rotate(context.camera.xRot, 1f, 0f, 0f)
        stack.scale(-0.025f, -0.025f, 0.025f)
        //#endif

        stack.translate(-5.0, -5.0, 0.0)

        vertices(context, 255, entry.light, entry, false)
        vertices(context, 40, entry.light, entry, true)

        stack.popPose()
    }

    private fun vertices(
        context: LevelRenderContext,
        alpha: Int,
        light: Int,
        entry: StaticSourceIconState.Entry,
        seeThrough: Boolean,
    ) {
        context.submitTexturedGeometry(entry.iconLocation, seeThrough) { pose, buffer ->
            vertex(pose, buffer, 0f, 10f, 0f, 0f, 1f, alpha, light)
            vertex(pose, buffer, 10f, 10f, 0f, 1f, 1f, alpha, light)
            vertex(pose, buffer, 10f, 0f, 0f, 1f, 0f, alpha, light)
            vertex(pose, buffer, 0f, 0f, 0f, 0f, 0f, alpha, light)
        }
    }

    private fun vertex(
        pose: PoseStack.Pose,
        buffer: VertexConsumer,
        x: Float, y: Float, z: Float,
        u: Float, v: Float,
        alpha: Int, light: Int,
    ) {
        create(buffer)
            .position(pose, x, y, z)
            .color(255, 255, 255, alpha)
            .uv(u, v)
            .light(light)
            .normal(pose, 0f, 0f, -1f)
            .end()
    }
}

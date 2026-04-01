package su.plo.voice.client.render.level

import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import su.plo.lib.mod.client.render.level.LevelRenderStateHolder
import su.plo.voice.api.client.audio.source.ClientAudioSource
import su.plo.voice.client.BaseVoiceClient
import su.plo.voice.client.audio.source.ClientStaticSource
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.client.event.LevelExtractRenderStateEvent
import su.plo.voice.client.extension.position
import su.plo.voice.proto.data.audio.source.StaticSourceInfo
import kotlin.math.exp
import kotlin.math.min

private const val LERP_SPEED = 5.0

class StaticSourceIconStateExtractor(
    private val voiceClient: BaseVoiceClient,
    private val config: VoiceClientConfig,
) : LevelExtractRenderStateEvent.Callback {

    override fun onExtract(
        level: ClientLevel,
        camera: Camera,
        delta: Float,
        state: LevelRenderStateHolder,
    ) {
        val iconState = extractRenderState(level, camera, delta)
        if (iconState != null) {
            state.set(STATIC_SOURCE_ICON_STATE_KEY, iconState)
        }
    }

    private fun extractRenderState(
        level: ClientLevel,
        camera: Camera,
        delta: Float,
    ): StaticSourceIconState? {
        if (isIconHidden()) return null
        if (!config.overlay.showStaticSourceIcons.value()) return null

        val entries = voiceClient.sourceManager.sources
            .filter {
                it.sourceInfo is StaticSourceInfo &&
                        it.sourceInfo.isIconVisible &&
                        it.isActivated()
            }
            .map { it as ClientStaticSource }
            .mapNotNull { source ->
                val sourceLine = voiceClient.sourceLineManager
                    .getLineById(source.sourceInfo.lineId)
                    .orElse(null) ?: return@mapNotNull null

                val target = source.sourceInfo.position
                val renderPos = source.lastRenderPosition

                if (target.distanceSquared(renderPos) > 1.0) {
                    renderPos.x = target.x
                    renderPos.y = target.y
                    renderPos.z = target.z
                } else {
                    val t = min(1.0, 1.0 - exp(-LERP_SPEED * delta))
                    renderPos.x = Mth.lerp(t, renderPos.x, target.x)
                    renderPos.y = Mth.lerp(t, renderPos.y, target.y)
                    renderPos.z = Mth.lerp(t, renderPos.z, target.z)
                }

                val cameraPos = camera.position()
                if (cameraPos.distanceToSqr(renderPos.x, renderPos.y, renderPos.z) > 4096.0)
                    return@mapNotNull null

                val blockPos = BlockPos(
                    renderPos.x.toInt(),
                    renderPos.y.toInt(),
                    renderPos.z.toInt(),
                )

                val light = LevelRenderer.getLightColor(level, blockPos)

                val iconLocation = ResourceLocation.tryParse(sourceLine.icon)
                    ?: return@mapNotNull null

                StaticSourceIconState.Entry(
                    renderPos.x,
                    renderPos.y,
                    renderPos.z,
                    light,
                    iconLocation,
                )
            }

        if (entries.isEmpty()) return null
        return StaticSourceIconState(entries)
    }

    private fun isIconHidden(): Boolean {
        if (!voiceClient.serverInfo.isPresent || !voiceClient.udpClientManager.isConnected) return true

        val showIcons = config.overlay.showSourceIcons.value()
        return showIcons == 2 || (net.minecraft.client.Minecraft.getInstance().options.hideGui && showIcons == 0)
    }
}

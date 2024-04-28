package su.plo.voice.client.render.voice

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.texture.ModPlayerSkins
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.line.ClientSourceLine
import su.plo.voice.api.client.config.overlay.OverlayPosition
import su.plo.voice.api.client.config.overlay.OverlaySourceState
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.client.event.render.HudRenderEvent
import su.plo.voice.proto.data.audio.source.DirectSourceInfo
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo
import su.plo.voice.proto.data.audio.source.SourceInfo
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution.scaledHeight
import gg.essential.universal.UResolution.scaledWidth
import java.util.*

class OverlayRenderer(
    private val voiceClient: PlasmoVoiceClient,
    private val config: VoiceClientConfig
) {

    @EventSubscribe
    fun onHudRender(event: HudRenderEvent) {
        if (!voiceClient.serverInfo.isPresent ||
            !voiceClient.udpClientManager.client.isPresent ||
            Minecraft.getInstance().player == null ||
            !config.overlay.overlayEnabled.value()
        ) return

        val position = config.overlay.overlayPosition.value()
        var renderedIndex = 0

        for (sourceLine in voiceClient.sourceLineManager.lines) {
            val sourceState = config.overlay.sourceStates.getState(sourceLine).value()
            if (sourceState == OverlaySourceState.OFF || sourceState == OverlaySourceState.NEVER) continue

            val toRender: MutableList<Pair<UUID, RenderSourceInfo>> = Lists.newArrayList()

            if (sourceLine.hasPlayers() && sourceState == OverlaySourceState.ALWAYS) {
                val toRenderByPlayerId: MutableMap<UUID, RenderSourceInfo> = Maps.newHashMap()

                for (player in sourceLine.players!!.sortedBy { it.name }) {
                    val renderSourceInfo = RenderSourceInfo(
                        player.id,
                        McTextComponent.literal(player.name),
                        player,
                        false
                    )

                    toRenderByPlayerId[player.id] = renderSourceInfo
                    toRender.add(
                        Pair(
                            player.id,
                            renderSourceInfo
                        )
                    )
                }

                for (selfSource in voiceClient.sourceManager.allSelfSourceInfos) {
                    val selfSourceInfo = selfSource.selfSourceInfo
                    if (selfSourceInfo.sourceInfo.lineId != sourceLine.id) continue

//                val activated = voiceClient.activationManager
//                    .getActivationById(selfSourceInfo.activationId)
//                    .orElse(null)?.isActivated
//                    ?: false

                    toRenderByPlayerId[selfSourceInfo.playerId]?.apply {
                        activated = voiceClient.activationManager
                            .getActivationById(selfSourceInfo.activationId)
                            .orElse(null)?.isActive
                            ?: false
                    }
                    // todo: WHEN_TALKING?
//                ?: run {
//                val sourceInfo = selfSourceInfo.sourceInfo
//                if (sourceState == OverlaySourceState.WHEN_TALKING && sourceInfo is DirectSourceInfo) {
//                    val player = sourceLine.players.find { it.id == sourceInfo.sender?.id } ?: return@run
//                    renderPlayer(player, true)
//                }
//            }
                }

                for (source in voiceClient.sourceManager.getSourcesByLineId(sourceLine.id)) {
                    if (!source.canHear()) continue

                    val sourceInfo = source.sourceInfo as? DirectSourceInfo ?: continue

                    toRenderByPlayerId[sourceInfo.sender?.id]?.apply {
                        activated = true
                    }
                }
            } else {
                for (source in voiceClient.sourceManager.getSourcesByLineId(sourceLine.id)) {
                    if (!source.canHear()) continue

                    val sourceId = getSourceSenderId(source.sourceInfo)

                    toRender.add(
                        Pair(
                            sourceId,
                            RenderSourceInfo(
                                getSourceSenderId(source.sourceInfo),
                                getSourceSenderName(source.sourceInfo, sourceLine),
                                getSourcePlayer(source.sourceInfo),
                                true
                            )
                        )
                    )
                }
            }

            for ((_, sourceInfo) in toRender) {
                renderEntry(event.stack, sourceLine, position, renderedIndex++, sourceInfo)
            }
        }
    }

    private fun renderEntry(
        stack: UMatrixStack,
        sourceLine: ClientSourceLine,
        position: OverlayPosition,
        index: Int,
        sourceInfo: RenderSourceInfo
    ) {
        if (Minecraft.getInstance().level == null) return

        val overlayStyle = config.overlay.overlayStyle.value()

        // todo: entity renderer?
        val sourceName = sourceInfo.sourceName
        val textWidth = RenderUtil.getTextWidth(sourceName) + 8
        var x = calcPositionX(position.x)
        var y = calcPositionY(position.y)
        if (position.isBottom) {
            y -= (ENTRY_HEIGHT + 1) * (index + 1)
        } else {
            y += (ENTRY_HEIGHT + 1) * index
        }

        UGraphics.depthFunc(515)

        stack.push()
        stack.translate(0f, 0f, 1000f)

//        int backgroundColor = minecraft.getOptions().getBackgroundColor(Integer.MIN_VALUE);
        val backgroundColor = (0.25f * 255.0f).toInt() shl 24

        // render helm
        if (overlayStyle.hasSkin) {
            sourceInfo.player?.let {
                if (position.isRight) {
                    x -= 16
                }

                UGraphics.bindTexture(0, loadSkin(it))
                UGraphics.color4f(1f, 1f, 1f, 1f)
                RenderUtil.blit(stack, x, y, 16, 16, 8f, 8f, 8, 8, 64, 64)
                UGraphics.enableBlend()
                RenderUtil.blit(stack, x, y, 16, 16, 40f, 8f, 8, 8, 64, 64)
                UGraphics.disableBlend()
                if (!position.isRight) {
                    x += 16 + 1
                }
            }
        }

        // render text
        if (overlayStyle.hasName) {
            if (position.isRight) {
                x -= textWidth + 1
            }

            RenderUtil.fill(stack, x, y, x + textWidth, y + ENTRY_HEIGHT, backgroundColor)
            RenderUtil.drawString(stack, sourceName, x + 4, y + 4, 0xFFFFFF, false)

            if (sourceInfo.activated && !position.isRight) {
                x += textWidth + 1
            }
        }

        // render line icon
        if (sourceInfo.activated) {
            if (position.isRight) {
                x -= 16 + 1
            }

            RenderUtil.fill(stack, x, y, x + 16, y + ENTRY_HEIGHT, backgroundColor)
            UGraphics.bindTexture(0, ResourceLocation(sourceLine.icon))
            UGraphics.color4f(1f, 1f, 1f, 1f)

            UGraphics.enableBlend()
            RenderUtil.blit(stack, x, y, 0, 0f, 0f, 16, 16, 16, 16)
            UGraphics.disableBlend()
        }

        stack.pop()
    }

    private fun getSourceSenderName(
        sourceInfo: SourceInfo,
        sourceLine: ClientSourceLine
    ): McTextComponent {
        if (sourceInfo.name != null) {
            var sourceName = sourceInfo.name!!
            if (sourceName.length > MAX_TEXT_WIDTH) {
                sourceName = sourceName.substring(0, MAX_TEXT_WIDTH) + "..."
            }

            return McTextComponent.literal(sourceName)
        }

        return when (sourceInfo) {
            is DirectSourceInfo -> {
                sourceInfo.sender?.let {
                    McTextComponent.literal(it.name)
                } ?: sourceLine.translationComponent
            }

            is PlayerSourceInfo -> {
                Minecraft.getInstance().connection?.getPlayerInfo(sourceInfo.playerInfo.playerId)?.let {
                    McTextComponent.literal(it.profile.name)
                } ?: sourceLine.translationComponent
            }

            else -> {
                sourceLine.translationComponent
            }
        }
    }

    private fun getSourceSenderId(sourceInfo: SourceInfo): UUID =
        when (sourceInfo) {
            is DirectSourceInfo ->
                sourceInfo.sender?.id ?: sourceInfo.id

            is PlayerSourceInfo ->
                sourceInfo.playerInfo.playerId

            else -> sourceInfo.id
        }

    private fun getSourcePlayer(sourceInfo: SourceInfo): McGameProfile? =
        when (sourceInfo) {
            is DirectSourceInfo ->
                sourceInfo.sender

            is PlayerSourceInfo ->
                McGameProfile(
                    sourceInfo.playerInfo.playerId,
                    sourceInfo.playerInfo.playerNick,
                    Collections.emptyList()
                )

            else -> null
        }

//        return when(sourceInfo) {
//            is DirectSourceInfo -> {
//                sourceInfo.sender?.let {
//                    loadSkin(it)
//                } ?: ModPlayerSkins.getDefaultSkin(sourceInfo.id)
//            }
//
//            is PlayerSourceInfo -> {
//                loadSkin(sourceInfo.playerInfo.playerId, sourceInfo.playerInfo.playerNick)
//            }
//
//            else -> ModPlayerSkins.getDefaultSkin(sourceInfo.id)
//        }

    private fun loadSkin(gameProfile: McGameProfile): ResourceLocation {
        ModPlayerSkins.loadSkin(gameProfile)
        return ModPlayerSkins.getSkin(gameProfile.id, gameProfile.name)
    }

    private fun loadSkin(playerId: UUID, playerName: String): ResourceLocation {
        ModPlayerSkins.loadSkin(
            playerId,
            playerName,
            null
        )
        return ModPlayerSkins.getSkin(playerId, playerName)
    }

    private fun calcPositionX(x: Int): Int {
        return if (x < 0) {
            scaledWidth + x
        } else {
            x
        }
    }

    private fun calcPositionY(y: Int): Int {
        return if (y < 0) {
            scaledHeight + y
        } else {
            y
        }
    }

    companion object {
        private const val ENTRY_HEIGHT = 16
        private const val MAX_TEXT_WIDTH = 40
    }
}

private data class RenderSourceInfo(
    val sourceId: UUID,
    val sourceName: McTextComponent,
    val player: McGameProfile?,
    var activated: Boolean
)

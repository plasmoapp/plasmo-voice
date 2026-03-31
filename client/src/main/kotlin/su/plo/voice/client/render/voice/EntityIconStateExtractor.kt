package su.plo.voice.client.render.voice

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.line.ClientSourceLine
import su.plo.voice.api.client.audio.source.ClientAudioSource
import su.plo.voice.client.ModVoiceClient
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.client.extension.toVec3
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.data.config.PlayerIconVisibility
import su.plo.voice.proto.data.player.VoicePlayerInfo
import java.util.EnumSet
import kotlin.collections.plus
import kotlin.jvm.optionals.getOrNull
import kotlin.math.roundToInt

object EntityIconStateExtractor {

    private val voiceClient by lazy { ModVoiceClient.INSTANCE }
    private val config by lazy { voiceClient.config }
    private val volumeAction by lazy { voiceClient.volumeAction }

    private val minecraft by lazy { Minecraft.getInstance() }

    @JvmStatic
    fun extract(entity: LivingEntity): EntityVoiceIconState? {
        if (isIconHidden(voiceClient, config)) return null

        val clientPlayer = minecraft.player ?: return null

        val customName = entity.customName?.toString()

        val shouldHideIcon = customName?.contains("plasmo-voice.hide-all-icons") == true

        if (shouldHideIcon || entity.isInvisibleTo(clientPlayer)) return null

        return if (entity.type == EntityType.PLAYER) {
            extractPlayer(entity, clientPlayer)
        } else {
            extractLivingEntity(entity)
        }
    }

    private fun extractPlayer(
        entity: LivingEntity,
        clientPlayer: LocalPlayer,
    ): EntityVoiceIconState? {
        val connection = voiceClient.serverConnection.orElse(null) ?: return null

        val isFakePlayer = !minecraft
            .connection!!
            .onlinePlayerIds
            .contains(entity.uuid)

        val entitySources = voiceClient.sourceManager.getEntitySources(entity.id)
        if (isFakePlayer && entitySources.isNotEmpty()) {
            return extractLivingEntity(entity)
        }

        if (entity.uuid == clientPlayer.uuid || isFakePlayer) {
            return null
        }

        val percentText = if (volumeAction.isShown(entity.uuid)) {
            val volume = config.voice.volumes.getVolume("source_${entity.uuid}")
            val percent = (volume.value() * 100.0).roundToInt()
            McTextComponent.literal("$percent%")
        } else null

        val playerInfo = connection.getPlayerById(entity.uuid).orElse(null)
        val iconLocation = getPlayerIcon(entity, playerInfo)

        if (iconLocation == null && percentText == null) return null

        return EntityVoiceIconState(
            iconLocation = iconLocation?.let { ResourceLocation.tryParse(it) },
            iconOffset = getPlayerIconOffset(entity),
            percentText = percentText,
        )
    }

    private fun extractLivingEntity(entity: LivingEntity): EntityVoiceIconState? {
        if (!voiceClient.serverConnection.isPresent) return null

        val sources = voiceClient.sourceManager.getEntitySources(entity.id)
        val highestSourceLine = getHighestActivatedSourceLine(voiceClient, sources) ?: return null

        return EntityVoiceIconState(
            iconLocation = ResourceLocation.tryParse(highestSourceLine.icon),
            iconOffset = getPlayerIconOffset(entity),
            percentText = null,
        )
    }

    private fun isIconHidden(voiceClient: PlasmoVoiceClient, config: VoiceClientConfig): Boolean {
        if (!voiceClient.serverInfo.isPresent || !voiceClient.udpClientManager.isConnected) return true
        val showIcons = config.overlay.showSourceIcons.value()
        return showIcons == 2 || (minecraft.options.hideGui && showIcons == 0)
    }

    private fun getPlayerIcon(
        entity: LivingEntity,
        playerInfo: VoicePlayerInfo?,
    ): String? {
        val iconVisibility = getPlayerIconVisibility(entity)

        // not installed
        if (playerInfo == null) {
            // fallback to entity/player source if we don't have a player in connection list
            val iconBySource = getPlayerIconBySource(entity, iconVisibility)
            if (iconBySource != null) return iconBySource
            return iconOrNull(
                iconVisibility,
                PlayerIconVisibility.HIDE_NOT_INSTALLED,
                "plasmovoice:textures/icons/headset_not_installed.png",
            )
        }

        // client mute
        if (config.voice.volumes.getMute("source_${playerInfo.playerId}").value()) {
            return iconOrNull(
                iconVisibility,
                PlayerIconVisibility.HIDE_CLIENT_MUTED,
                "plasmovoice:textures/icons/speaker_disabled.png",
            )
        }

        // server mute
        if (playerInfo.isMuted) {
            return iconOrNull(
                iconVisibility,
                PlayerIconVisibility.HIDE_SERVER_MUTED,
                "plasmovoice:textures/icons/speaker_muted.png",
            )
        }

        // client disabled voicechat
        if (playerInfo.isVoiceDisabled) {
            return iconOrNull(
                iconVisibility,
                PlayerIconVisibility.HIDE_VOICE_CHAT_DISABLED,
                "plasmovoice:textures/icons/headset_disabled.png",
            )
        }

        return getPlayerIconBySource(entity, iconVisibility)
    }

    private fun getPlayerIconBySource(
        entity: LivingEntity,
        iconVisibility: Set<PlayerIconVisibility>,
    ): String? {
        if (iconVisibility.contains(PlayerIconVisibility.HIDE_SOURCE_ICON)) return null

        val playerSources = voiceClient.sourceManager.getPlayerSources(entity.uuid)
        val entitySources = voiceClient.sourceManager.getEntitySources(entity.id)

        if (playerSources.isEmpty() && entitySources.isEmpty()) return null

        var highestSourceLine = getHighestActivatedSourceLine(voiceClient, playerSources)
        if (highestSourceLine == null) {
            highestSourceLine = getHighestActivatedSourceLine(voiceClient, entitySources)
            if (highestSourceLine == null) return null
        }

        return highestSourceLine.icon
    }

    private fun <T : SourceInfo> getHighestActivatedSourceLine(
        voiceClient: PlasmoVoiceClient,
        sources: Collection<ClientAudioSource<T>>,
    ): ClientSourceLine? =
        sources
            .filter { it.isActivated() && it.sourceInfo.isIconVisible }
            .mapNotNull { voiceClient.sourceLineManager.getLineById(it.sourceInfo.lineId).orElse(null) }
            .maxByOrNull { it.weight }

    private fun iconOrNull(
        iconVisibility: Set<PlayerIconVisibility>,
        target: PlayerIconVisibility,
        iconLocation: String,
    ): String? = if (iconVisibility.contains(target)) null else iconLocation

    private fun getPlayerIconOffset(entity: LivingEntity): Vec3 {
        if (entity !is Player) return Vec3.ZERO
        val serverInfo = ModVoiceClient.INSTANCE.serverInfo.getOrNull() ?: return Vec3.ZERO

        return serverInfo.playerIconOffset.toVec3()
    }

    private fun getPlayerIconVisibility(entity: LivingEntity): Set<PlayerIconVisibility> {
        if (entity !is Player) return PlayerIconVisibility.none()

        val serverInfo = ModVoiceClient.INSTANCE.serverInfo.getOrNull() ?: return PlayerIconVisibility.none()

        val customName = entity.customName?.toString()

        var playerIconVisibility = serverInfo.getPlayerIconVisibility()
        if (customName?.contains("plasmo-voice.hide-not-installed-icon") == true) {
            playerIconVisibility = EnumSet.copyOf(
                playerIconVisibility + setOf(PlayerIconVisibility.HIDE_NOT_INSTALLED)
            )
        }

        return playerIconVisibility
    }
}

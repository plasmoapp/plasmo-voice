package su.plo.voice.api.server.audio.capture

import com.google.common.collect.Maps
import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.event.audio.capture.ServerActivationRegisterEvent
import su.plo.voice.api.server.event.audio.capture.ServerActivationUnregisterEvent
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.event.player.PlayerActivationDistanceUpdateEvent
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.capture.VoiceActivation
import su.plo.voice.proto.data.audio.line.VoiceSourceLine
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import java.util.*
import java.util.function.Consumer

abstract class BaseProximityServerActivation(
    protected val voiceServer: PlasmoVoiceServer,
    protected val activationName: String,
    protected val defaultPermission: PermissionDefault
) {

    protected val activationId = VoiceActivation.generateId(activationName)
    protected val sourceLineId = VoiceSourceLine.generateId(activationName)
    protected val selfActivationInfo = SelfActivationInfo(voiceServer.udpConnectionManager)

    private val sourceByPlayerId: MutableMap<UUID, ServerPlayerSource> = Maps.newConcurrentMap()

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onActivationRegister(event: ServerActivationRegisterEvent) {
        val activation = event.activation
        if (activation.name != activationName) return

        activation.permissions.forEach { permission ->
            voiceServer.minecraftServer
                .permissionsManager
                .register(permission, defaultPermission)
        }
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onActivationUnregister(event: ServerActivationUnregisterEvent) {
        val activation = event.activation
        if (activation.name != activationName) return

        activation.permissions.forEach { permission ->
            voiceServer.minecraftServer
                .permissionsManager
                .unregister(permission)
        }
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onSourceSendPacket(event: ServerSourcePacketEvent) {
        val source = event.source as? ServerPlayerSource ?: return

        if (!selfActivationInfo.lastPlayerActivationIds
                .containsKey(source.player.instance.uuid)
        ) {
            return
        }

        if (event.packet is SourceInfoPacket) {
            selfActivationInfo.updateSelfSourceInfo(
                source.player,
                source,
                (event.packet as SourceInfoPacket).sourceInfo
            )
        } else if (event.packet is SourceAudioEndPacket) {
            source.player.sendPacket(event.packet)
        }
    }

    @EventSubscribe
    fun onActivationDistanceChange(event: PlayerActivationDistanceUpdateEvent) {
        if (event.activation.id != VoiceActivation.PROXIMITY_ID) return
        if (event.oldDistance == -1) return
        event.player.visualizeDistance(event.distance)
    }

    @EventSubscribe
    fun onClientDisconnected(event: UdpClientDisconnectedEvent) =
        sourceByPlayerId.remove(event.connection.player.instance.uuid)

    protected fun sendAudioEndPacket(
        source: ServerPlayerSource,
        packet: PlayerAudioEndPacket,
        distance: Short = packet.distance
    ) {
        source.sendPacket(
            SourceAudioEndPacket(source.id, packet.sequenceNumber),
            distance
        )
    }

    protected fun sendAudioPacket(
        player: VoiceServerPlayer,
        source: ServerPlayerSource,
        packet: PlayerAudioPacket,
        distance: Short = packet.distance
    ) {
        val sourcePacket = SourceAudioPacket(
            packet.sequenceNumber, source.state.toByte(),
            packet.data,
            source.id,
            distance
        )

        if (source.sendAudioPacket(sourcePacket, distance, packet.activationId)) {
            selfActivationInfo.sendAudioInfo(player, source, packet.activationId, sourcePacket)
        }
    }

    protected fun getPlayerSource(
        player: VoiceServerPlayer,
        activationId: UUID,
        isStereo: Boolean?
    ): ServerPlayerSource? {
        if (activationId != this.activationId) return null

        val activation = voiceServer.activationManager
            .getActivationById(activationId)
            .orElse(null)
            ?: return null
        val sourceLine = voiceServer.sourceLineManager
            .getLineById(sourceLineId)
            .orElse(null)
            ?: return null

        return sourceByPlayerId.getOrPut(player.instance.uuid) {
            voiceServer.sourceManager.createPlayerSource(
                voiceServer,
                player,
                sourceLine,
                "opus",
                false
            )
        }.apply {
            line = sourceLine
        }.apply {
            isStereo?.let { isStereo ->
                setStereo(isStereo && activation.isStereoSupported)
            }
        }
    }
}

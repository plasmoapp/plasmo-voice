package su.plo.voice.api.server.audio.capture

import com.google.common.collect.Maps
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import java.util.*

/**
 * Helper class for proximity activations
 */
class ProximityServerActivationHelper(
    voiceServer: PlasmoVoiceServer,
    val activation: ServerActivation,
    val sourceLine: ServerSourceLine,
    private val distanceSupplier: DistanceSupplier? = null
) {

    private val selfActivationInfo = SelfActivationInfo(voiceServer.udpConnectionManager)

    private val sourceByPlayerId: MutableMap<UUID, ServerPlayerSource> = Maps.newConcurrentMap()

    init {
        activation.onPlayerActivation(this::onActivation)
        activation.onPlayerActivationEnd(this::onActivationEnd)
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
        }
    }

    @EventSubscribe
    fun onClientDisconnected(event: UdpClientDisconnectedEvent) =
        sourceByPlayerId.remove(event.connection.player.instance.uuid)

    private fun onActivation(player: VoicePlayer, packet: PlayerAudioPacket): ServerActivation.Result {
        getPlayerSource(player as VoiceServerPlayer, packet.isStereo).also {
            val distance = distanceSupplier?.getDistance(player, packet) ?: packet.distance
            if (sendAudioPacket(player, it, packet, distance)) {
                return ServerActivation.Result.HANDLED
            }
        }

        return ServerActivation.Result.IGNORED
    }

    private fun onActivationEnd(player: VoicePlayer, packet: PlayerAudioEndPacket): ServerActivation.Result {
        getPlayerSource(player as VoiceServerPlayer).also {
            val distance = distanceSupplier?.getDistance(player, packet) ?: packet.distance
            if (sendAudioEndPacket(it, packet, distance)) {
                return ServerActivation.Result.HANDLED
            }
        }

        return ServerActivation.Result.IGNORED
    }

    private fun sendAudioEndPacket(
        source: ServerPlayerSource,
        packet: PlayerAudioEndPacket,
        distance: Short = packet.distance
    ): Boolean {
        val sourceEndPacket = SourceAudioEndPacket(source.id, packet.sequenceNumber)

        if (source.sendPacket(sourceEndPacket, distance)) {
            source.player.sendPacket(sourceEndPacket)
            return true
        }

        return false
    }

    private fun sendAudioPacket(
        player: VoiceServerPlayer,
        source: ServerPlayerSource,
        packet: PlayerAudioPacket,
        distance: Short = packet.distance
    ): Boolean {
        val sourcePacket = SourceAudioPacket(
            packet.sequenceNumber, source.state.toByte(),
            packet.data,
            source.id,
            distance
        )

        if (source.sendAudioPacket(sourcePacket, distance, packet.activationId)) {
            selfActivationInfo.sendAudioInfo(player, source, packet.activationId, sourcePacket)
            return true
        }

        return false
    }

    private fun getPlayerSource(
        player: VoiceServerPlayer,
        isStereo: Boolean? = null
    ): ServerPlayerSource {
        return sourceByPlayerId.getOrPut(player.instance.uuid) {
            sourceLine.createPlayerSource(player)
        }.apply {
            isStereo?.let { isStereo ->
                setStereo(isStereo && activation.isStereoSupported)
            }
        }
    }

    interface DistanceSupplier {

        fun getDistance(player: VoiceServerPlayer, packet: PlayerAudioPacket): Short

        fun getDistance(player: VoiceServerPlayer, packet: PlayerAudioEndPacket): Short
    }
}

package su.plo.voice.api.server.audio.capture

import com.google.common.collect.Maps
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import java.util.*

/**
 * Helper class for proximity activations
 *
 * Automatically creates player sources in [getPlayerSource]
 *
 * Uses [SelfActivationInfo] for [sendAudioPacket] and [sendAudioEndPacket]
 */
class ProximityServerActivationHelper(
    voiceServer: PlasmoVoiceServer,
    val activation: ServerActivation,
    val sourceLine: ServerSourceLine
) {

    private val selfActivationInfo = SelfActivationInfo(voiceServer.udpConnectionManager)

    private val sourceByPlayerId: MutableMap<UUID, ServerPlayerSource> = Maps.newConcurrentMap()

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

    fun sendAudioEndPacket(
        source: ServerPlayerSource,
        packet: PlayerAudioEndPacket,
        distance: Short = packet.distance
    ) {
        val sourceEndPacket = SourceAudioEndPacket(source.id, packet.sequenceNumber)

        if (source.sendPacket(sourceEndPacket, distance)) {
            source.player.sendPacket(sourceEndPacket)
        }
    }

    fun sendAudioPacket(
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

    fun getPlayerSource(
        player: VoiceServerPlayer,
        activationId: UUID,
        isStereo: Boolean?
    ): ServerPlayerSource? {
        if (activationId != activation.id) return null

        return sourceByPlayerId.getOrPut(player.instance.uuid) {
            sourceLine.createPlayerSource(player)
        }.apply {
            line = sourceLine
        }.apply {
            isStereo?.let { isStereo ->
                setStereo(isStereo && activation.isStereoSupported)
            }
        }
    }
}

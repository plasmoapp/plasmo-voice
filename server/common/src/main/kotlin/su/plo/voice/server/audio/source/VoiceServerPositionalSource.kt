package su.plo.voice.server.audio.source

import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerPositionalSource
import su.plo.voice.api.server.event.audio.source.ServerSourceAudioPacketEvent
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*

abstract class VoiceServerPositionalSource<S : SourceInfo>(
    private val voiceServer: PlasmoVoiceServer,
    addon: AddonContainer,
    id: UUID,
    line: ServerSourceLine,
    codec: String?,
    stereo: Boolean
) : BaseServerAudioSource<S>(addon, id, line, codec, stereo), ServerPositionalSource<S> {

    private val playerPosition = ServerPos3d()

    override fun sendAudioPacket(packet: SourceAudioPacket, distance: Short): Boolean {
        return sendAudioPacket(packet, distance, null)
    }

    override fun sendAudioPacket(packet: SourceAudioPacket, distance: Short, activationId: UUID?): Boolean {
        // call event
        val event = ServerSourceAudioPacketEvent(this, packet, distance, activationId)
        if (!voiceServer.eventBus.call(event)) return false

        // update packet's source state
        packet.sourceState = state.get().toByte()

        val listenersDistance = event.distance * DISTANCE_MULTIPLIER

        // update source info on listeners if source is dirty
        if (dirty.compareAndSet(true, false))
            sendPacket(SourceInfoPacket(sourceInfo), listenersDistance.toShort())

        val sourcePosition = position
        val distanceSquared = (listenersDistance * listenersDistance).toDouble()

        for (connection in voiceServer.udpConnectionManager.connections) {
            if (notMatchFilters(connection.player)) continue

            connection.player.instance.getServerPosition(playerPosition)
            if (sourcePosition.world == playerPosition.world &&
                sourcePosition.distanceSquared(playerPosition) <= distanceSquared
            ) {
                connection.sendPacket(packet)
            }
        }
        return true
    }

    override fun sendPacket(packet: Packet<*>, distance: Short): Boolean {
        // call event
        val event = ServerSourcePacketEvent(this, packet, distance)
        if (!voiceServer.eventBus.call(event)) return false

        val listenersDistance = event.distance * DISTANCE_MULTIPLIER

        val sourcePosition = position
        val distanceSquared = (listenersDistance * listenersDistance).toDouble()

        for (connection in voiceServer.udpConnectionManager.connections) {
            if (notMatchFilters(connection.player)) continue

            connection.player.instance.getServerPosition(playerPosition)
            if (sourcePosition.world == playerPosition.world &&
                sourcePosition.distanceSquared(playerPosition) <= distanceSquared
            ) {
                connection.player.sendPacket(packet)
            }
        }
        return true
    }

    companion object {

        private const val DISTANCE_MULTIPLIER = 2
    }
}

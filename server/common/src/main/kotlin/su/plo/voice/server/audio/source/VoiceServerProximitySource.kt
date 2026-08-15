package su.plo.voice.server.audio.source

import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.capture.PlayerActivationInfo
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.provider.AudioFrameProvider
import su.plo.voice.api.server.audio.source.AudioSender
import su.plo.voice.api.server.audio.source.ServerProximitySource
import su.plo.voice.api.server.event.audio.source.ServerSourceAudioPacketEvent
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent
import su.plo.voice.api.server.socket.UdpServerConnection
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.UUID
import java.util.function.Supplier
import kotlin.math.min

abstract class VoiceServerProximitySource<S : SourceInfo>(
    private val voiceServer: PlasmoVoiceServer,
    addon: AddonContainer,
    id: UUID,
    private val serverSourceLine: ServerSourceLine,
    decoderInfo: CodecInfo?,
    stereo: Boolean,
) : BaseServerAudioSource<S>(addon, id, serverSourceLine, decoderInfo, stereo), ServerProximitySource<S> {

    override var angle: Int = 0
        get() = field
        set(value) {
            field = value
        }

    override fun sendAudioPacket(
        packet: SourceAudioPacket,
        distance: Short,
        activationInfo: PlayerActivationInfo?,
    ): Boolean {
        val event = ServerSourceAudioPacketEvent(this, packet, distance, activationInfo)
        if (!voiceServer.eventBus.fire(event)) return false
        if (event.result == ServerSourceAudioPacketEvent.Result.HANDLED) return true

        // update packet's source state
        packet.sourceState = state.get().toByte()

        // update source info on listeners if source is dirty
        if (dirty.compareAndSet(true, false)) {
            resolveSourceInfo()
                .thenAccept { sendPacket(SourceInfoPacket(it), event.distance) }
        }

        getListeners(event.distance).forEach { connection ->
            connection.sendPacket(packet)
        }

        return true
    }

    override fun sendPacket(packet: Packet<*>, distance: Short): Boolean {
        val event = ServerSourcePacketEvent(this, packet, distance)
        if (!voiceServer.eventBus.fire(event)) return false
        if (event.result == ServerSourcePacketEvent.Result.HANDLED) return true

        getListeners(event.distance).forEach { connection ->
            connection.player.sendPacket(packet)
        }

        return true
    }

    override fun getLine(): ServerSourceLine =
        serverSourceLine

    override fun createAudioSender(frameProvider: AudioFrameProvider, distanceProvider: Supplier<Short>): AudioSender =
        AudioSender(
            frameProvider,
            { frame, sequenceNumber -> sendAudioFrame(frame, sequenceNumber, distanceProvider.get()) },
            { sequenceNumber -> sendAudioEnd(sequenceNumber, distanceProvider.get()) }
        )

    override fun getListeners(distance: Short): Iterable<UdpServerConnection> {
        val listenersDistance = min(
            distance + voiceServer.config!!.voice().maxExtraAudioBroadcastDistance(),
            distance * DISTANCE_MULTIPLIER,
        )

        val distanceSquared = (listenersDistance * listenersDistance).toDouble()

        return Iterable {
            val sourcePosition = position
            val playerPosition = ServerPos3d()

            voiceServer.udpConnectionManager.connections
                .asSequence()
                .filter { matchFilters(it.player) }
                .filter { connection ->
                    connection.player.instance.getServerPosition(playerPosition)

                    sourcePosition.world == playerPosition.world && sourcePosition.distanceSquared(playerPosition) <= distanceSquared
                }
                .iterator()
        }
    }

    companion object {

        private const val DISTANCE_MULTIPLIER = 2
    }
}

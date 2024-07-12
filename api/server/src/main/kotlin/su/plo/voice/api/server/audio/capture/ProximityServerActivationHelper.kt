package su.plo.voice.api.server.audio.capture

import com.google.common.collect.Maps
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import java.util.*

/**
 * Helper class for proximity activations.
 *
 * This class will handle sources and activation manipulations for proximity activations.
 * You just need to create an instance of this class and register it in Plasmo Voice eventbus.
 *
 * Example usage:
 * ```java
 * ProximityServerActivationHelper proximityHelper = new ProximityServerActivationHelper(voiceServer, activation, sourceLine);
 * voiceServer.eventBus.register(voiceServer, proximityHelper);
 * ```
 */
class ProximityServerActivationHelper @JvmOverloads constructor(
    val voiceServer: PlasmoVoiceServer,
    val activation: ServerActivation,
    val sourceLine: ServerSourceLine,
    private val distanceSupplier: DistanceSupplier? = null
) {

    private val sourceByPlayerId: MutableMap<UUID, ServerPlayerSource> = Maps.newConcurrentMap()

    init {
        activation.onPlayerActivation(this::onActivation)
        activation.onPlayerActivationEnd(this::onActivationEnd)
    }

    fun registerListeners(addon: Any) {
        voiceServer.eventBus.register(addon, this)
    }

    fun unregisterListeners(addon: Any) {
        voiceServer.eventBus.unregister(addon, this)
    }

    @EventSubscribe
    fun onClientDisconnected(event: UdpClientDisconnectedEvent) =
        sourceByPlayerId.remove(event.connection.player.instance.uuid)?.remove()

    private fun onActivation(player: VoicePlayer, packet: PlayerAudioPacket): ServerActivation.Result {
        getPlayerSource(player as VoiceServerPlayer, packet.isStereo).also { source ->
            val distance = distanceSupplier?.getDistance(player, packet) ?: packet.distance

            val sourcePacket = SourceAudioPacket(
                packet.sequenceNumber, source.state.toByte(),
                packet.data,
                source.id,
                distance
            )

            val activationInfo = PlayerActivationInfo(player, packet)

            if (source.sendAudioPacket(sourcePacket, distance, activationInfo)) {
                return ServerActivation.Result.HANDLED
            }
        }

        return ServerActivation.Result.IGNORED
    }

    private fun onActivationEnd(player: VoicePlayer, packet: PlayerAudioEndPacket): ServerActivation.Result {
        getPlayerSource(player as VoiceServerPlayer).also { source ->
            val distance = distanceSupplier?.getDistance(player, packet) ?: packet.distance

            val sourceEndPacket = SourceAudioEndPacket(source.id, packet.sequenceNumber)

            if (source.sendPacket(sourceEndPacket, distance)) {
                return ServerActivation.Result.HANDLED
            }
        }

        return ServerActivation.Result.IGNORED
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

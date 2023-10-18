package su.plo.voice.server.audio.source

import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoBaseVoiceServer
import su.plo.voice.api.server.audio.line.BaseServerSourceLine
import su.plo.voice.api.server.audio.source.ServerDirectSource
import su.plo.voice.api.server.connection.UdpConnectionManager
import su.plo.voice.api.server.event.audio.source.ServerSourceAudioPacketEvent
import su.plo.voice.api.server.event.audio.source.ServerSourcePacketEvent
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.api.server.socket.UdpConnection
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*
import kotlin.jvm.optionals.getOrNull

class VoiceServerDirectSource(
    voiceServer: PlasmoBaseVoiceServer,
    udpConnections: UdpConnectionManager<out VoicePlayer, out UdpConnection>,
    addon: AddonContainer,
    line: BaseServerSourceLine,
    decoderInfo: CodecInfo?,
    stereo: Boolean,
    override val player: VoicePlayer
) : VoiceBaseServerDirectSource(voiceServer, udpConnections, addon, line, decoderInfo, stereo),
    ServerDirectSource {

    override fun sendAudioPacket(packet: SourceAudioPacket, activationId: UUID?): Boolean {
        val event = ServerSourceAudioPacketEvent(this, packet, activationId);
        if (!voiceServer.eventBus.fire(event)) return false

        packet.sourceState = state.get().toByte()

        if (dirty.compareAndSet(true, false)) {
            updateSourceInfo()
        }

        if (matchFilters(player)) {
            udpConnections.getConnectionByPlayerId(player.instance.uuid)
                .getOrNull()
                ?.sendPacket(packet)
        }

        return true
    }

    override fun sendPacket(packet: Packet<*>): Boolean {
        val event = ServerSourcePacketEvent(this, packet);
        if (!voiceServer.eventBus.fire(event)) return false

        if (matchFilters(player)) {
            player.sendPacket(packet)
        }

        return true
    }
}

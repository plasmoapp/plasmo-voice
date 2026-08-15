package su.plo.voice.server.audio.source

import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoBaseVoiceServer
import su.plo.voice.api.server.audio.line.BaseServerSourceLine
import su.plo.voice.api.server.audio.source.ServerBroadcastSource
import su.plo.voice.api.server.connection.UdpConnectionManager
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.api.server.socket.UdpConnection
import su.plo.voice.proto.data.audio.codec.CodecInfo
import kotlin.jvm.optionals.getOrNull

class VoiceServerBroadcastSource(
    voiceServer: PlasmoBaseVoiceServer,
    udpConnections: UdpConnectionManager<out VoicePlayer, out UdpConnection>,
    addon: AddonContainer,
    line: BaseServerSourceLine,
    decoderInfo: CodecInfo?,
    stereo: Boolean,
) : VoiceBaseServerDirectSource(voiceServer, udpConnections, addon, line, decoderInfo, stereo),
    ServerBroadcastSource {
    override var players: Collection<VoicePlayer>? = null

    override fun getListeners(): Iterable<UdpConnection> =
        Iterable {
            val sequence = this.players?.asSequence()
                ?.mapNotNull { player ->
                    udpConnections.getConnectionByPlayerId(player.instance.uuid).getOrNull()
                }
                ?: udpConnections.connections.asSequence()

            sequence.filter { matchFilters(it.player) }.iterator()
        }
}

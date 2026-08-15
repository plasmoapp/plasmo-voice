package su.plo.voice.server.audio.source

import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoBaseVoiceServer
import su.plo.voice.api.server.audio.line.BaseServerSourceLine
import su.plo.voice.api.server.audio.source.ServerDirectSource
import su.plo.voice.api.server.connection.UdpConnectionManager
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.api.server.socket.UdpConnection
import su.plo.voice.proto.data.audio.codec.CodecInfo
import kotlin.jvm.optionals.getOrNull

class VoiceServerDirectSource(
    voiceServer: PlasmoBaseVoiceServer,
    udpConnections: UdpConnectionManager<out VoicePlayer, out UdpConnection>,
    addon: AddonContainer,
    line: BaseServerSourceLine,
    decoderInfo: CodecInfo?,
    stereo: Boolean,
    override val player: VoicePlayer,
) : VoiceBaseServerDirectSource(voiceServer, udpConnections, addon, line, decoderInfo, stereo),
    ServerDirectSource {
    override fun getListeners(): Iterable<UdpConnection> =
        Iterable {
            val connection = udpConnections.getConnectionByPlayerId(player.instance.uuid)
                .getOrNull()
                ?.takeIf { matchFilters(player) }

            listOfNotNull(connection).iterator()
        }
}

package su.plo.voice.server.audio.line

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import su.plo.voice.api.server.PlasmoBaseVoiceServer
import su.plo.voice.api.server.audio.line.BaseServerSourceLine
import su.plo.voice.api.server.audio.line.BaseServerSourceLineManager
import su.plo.voice.api.server.connection.ConnectionManager
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.audio.line.VoiceSourceLine
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler
import su.plo.voice.proto.packets.tcp.clientbound.SourceLineUnregisterPacket
import java.util.*

abstract class VoiceBaseServerSourceLineManager<T : BaseServerSourceLine>(
    private val voiceServer: PlasmoBaseVoiceServer,
    private val tcpConnections: ConnectionManager<ClientPacketTcpHandler, out VoicePlayer>
) : BaseServerSourceLineManager<T> {

    protected val lineById: MutableMap<UUID, T> = Maps.newConcurrentMap()

    override val lines: Collection<T>
        get() = lineById.values

    override fun getLineById(id: UUID): Optional<T> =
        Optional.ofNullable(lineById[id])

    override fun getLineByName(name: String): Optional<T> =
        Optional.ofNullable(lineById[VoiceSourceLine.generateId(name)])

    override fun unregister(id: UUID): Boolean {
        lineById.remove(id)?.let { line ->
            line.clear()
            line.playersSets?.let { voiceServer.eventBus.unregister(voiceServer, it) }
            tcpConnections.broadcast(SourceLineUnregisterPacket(id))
            return true
        }

        return false
    }

    override fun unregister(name: String): Boolean =
        unregister(VoiceSourceLine.generateId(name))

    override fun clear() {
        Lists.newArrayList(lineById.values).forEach(this::unregister)
        lineById.clear()
    }
}

package su.plo.voice.api.server.audio.source

import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*

interface ServerPositionalSource<S : SourceInfo> : ServerAudioSource<S> {

    val position: ServerPos3d

    fun sendAudioPacket(packet: SourceAudioPacket, distance: Short): Boolean

    fun sendAudioPacket(packet: SourceAudioPacket, distance: Short, activationId: UUID?): Boolean

    fun sendPacket(packet: Packet<*>, distance: Short): Boolean

    override fun getLine(): ServerSourceLine
}

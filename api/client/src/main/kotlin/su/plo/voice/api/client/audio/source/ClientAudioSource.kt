package su.plo.voice.api.client.audio.source

import su.plo.voice.api.audio.source.AudioSource
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.device.source.SourceGroup
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*

interface ClientAudioSource<S : SourceInfo> : AudioSource<S> {

    var closeTimeoutMs: Long
    var sourceGroup: SourceGroup

    @Throws(DeviceException::class)
    fun update(sourceInfo: S)

    fun process(packet: SourceAudioPacket)

    fun process(packet: SourceAudioEndPacket)

    fun isClosed(): Boolean

    fun isActivated(): Boolean

    /**
     * Can hear this source
     * @return return true if you are in the reach of the source, and source is activated
     */
    fun canHear(): Boolean

    fun close()
}

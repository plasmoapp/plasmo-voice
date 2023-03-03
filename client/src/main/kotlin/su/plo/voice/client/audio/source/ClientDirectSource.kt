package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import su.plo.config.entry.BooleanConfigEntry
import su.plo.config.entry.DoubleConfigEntry
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.AlAudioDevice
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.device.source.AlSource
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.proto.data.audio.source.DirectSourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket

class ClientDirectSource(
    voiceClient: PlasmoVoiceClient,
    config: ClientConfig,
    sourceInfo: DirectSourceInfo
) : BaseClientAudioSource<DirectSourceInfo>(voiceClient, config, sourceInfo) {

    override var sourceVolume: DoubleConfigEntry = createSourceVolume(sourceInfo)

    private var sourceMute: BooleanConfigEntry? = createSourceMute(sourceInfo)

    @Throws(DeviceException::class)
    override fun update(sourceInfo: DirectSourceInfo) {
        if (sourceInfo.sender != this.sourceInfo.sender) {
            sourceVolume = createSourceVolume(sourceInfo)
            sourceMute = createSourceMute(sourceInfo)
        }

        super.update(sourceInfo)
        updateSourceParams()
    }

    override fun process(packet: SourceAudioPacket) {
        if (sourceMute?.value() == true) return
        super.process(packet)
    }

    override fun process(packet: SourceAudioEndPacket) {
        if (sourceMute?.value() == true) return
        super.process(packet)
    }

    override fun canHear(): Boolean {
        return isActivated()
    }

    override fun getPosition(position: FloatArray): FloatArray {
        if (sourceInfo.relativePosition != null) {
            if (sourceInfo.isCameraRelative) {
                position[0] = sourceInfo.relativePosition!!.x.toFloat()
                position[1] = sourceInfo.relativePosition!!.y.toFloat()
                position[2] = sourceInfo.relativePosition!!.z.toFloat()
            } else {
                return getAbsoluteSourcePosition(position)
            }
        } else {
            position[0] = 0f
            position[1] = 0f
            position[2] = 0f
        }

        return position
    }

    override fun getLookAngle(lookAngle: FloatArray): FloatArray {
        // todo: lookAngle?
        lookAngle[0] = 0f
        lookAngle[1] = 0f
        lookAngle[2] = 0f
        return lookAngle
    }

    override fun shouldCalculateOcclusion(): Boolean {
        return false // todo: relative position occlusion
    }

    override fun isPanningDisabled(): Boolean {
        return sourceInfo.isCameraRelative || super.isPanningDisabled()
    }

    private fun createSourceMute(sourceInfo: DirectSourceInfo): BooleanConfigEntry? =
        sourceInfo.sender?.let {
            config.voice
                .volumes
                .getMute("source_" + it.id)
        }

    private fun createSourceVolume(sourceInfo: DirectSourceInfo) =
        sourceInfo.sender?.let {
            config.voice
                .volumes
                .getVolume("source_${it.id}")
        } ?: super.sourceVolume

    private fun getAbsoluteSourcePosition(position: FloatArray): FloatArray {
        return sourceInfo.relativePosition?.let {
            val player: LocalPlayer = Minecraft.getInstance().player ?: return position

            position[0] = (player.x + it.x).toFloat()
            position[1] = (player.y + player.eyeHeight + it.y).toFloat()
            position[2] = (player.z + it.z).toFloat()

            return position
        } ?: position
    }

    private fun updateSourceParams() {
        for (source in sourceGroup.sources) {
            if (source !is AlSource) continue

            val device = source.device as AlAudioDevice
            device.runInContext {
                source.setInt(
                    0x202,  // AL_SOURCE_RELATIVE
                    if (sourceInfo.isCameraRelative) 1 else 0
                )
            }
        }
    }
}

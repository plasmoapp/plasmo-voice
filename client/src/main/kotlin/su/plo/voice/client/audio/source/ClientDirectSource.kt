package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.AlAudioDevice
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.device.source.AlSource
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.proto.data.audio.source.DirectSourceInfo

class ClientDirectSource(
    voiceClient: PlasmoVoiceClient,
    config: ClientConfig,
    sourceInfo: DirectSourceInfo
) : BaseClientAudioSource<DirectSourceInfo>(voiceClient, config, sourceInfo) {

    @Throws(DeviceException::class)
    override fun update(sourceInfo: DirectSourceInfo) {
        super.update(sourceInfo)
        updateSourceParams()
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

package su.plo.voice.client.audio.source

import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.proto.data.audio.source.DirectSourceInfo
import su.plo.voice.proto.data.audio.source.StaticSourceInfo

class ClientStaticSource(
    voiceClient: PlasmoVoiceClient,
    config: ClientConfig,
    sourceInfo: StaticSourceInfo
) : BaseClientAudioSource<StaticSourceInfo>(voiceClient, config, sourceInfo) {

    override fun getPosition(position: FloatArray): FloatArray {
        position[0] = sourceInfo.position.x.toFloat()
        position[1] = sourceInfo.position.y.toFloat()
        position[2] = sourceInfo.position.z.toFloat()
        return position
    }

    override fun getLookAngle(lookAngle: FloatArray): FloatArray {
        lookAngle[0] = sourceInfo.lookAngle.x.toFloat()
        lookAngle[1] = sourceInfo.lookAngle.y.toFloat()
        lookAngle[2] = sourceInfo.lookAngle.z.toFloat()
        return lookAngle
    }
}

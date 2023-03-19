package su.plo.voice.client.audio.source

import net.minecraft.world.phys.Vec3
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.client.utils.toVec3
import su.plo.voice.proto.data.audio.source.StaticSourceInfo

class ClientStaticSource(
    voiceClient: PlasmoVoiceClient,
    config: ClientConfig,
    sourceInfo: StaticSourceInfo
) : BaseClientAudioSource<StaticSourceInfo>(voiceClient, config, sourceInfo) {

    override fun getPosition(): Vec3 =
        sourceInfo.position.toVec3()

    override fun getLookAngle(): Vec3 =
        sourceInfo.lookAngle.toVec3()
}

package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import su.plo.lib.mod.extensions.eyePosition
import su.plo.voice.client.BaseVoiceClient
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.proto.data.audio.source.EntitySourceInfo

class ClientEntitySource(
    voiceClient: BaseVoiceClient,
    config: VoiceClientConfig,
    sourceInfo: EntitySourceInfo
) : BaseClientAudioSource<EntitySourceInfo>(voiceClient, config, sourceInfo) {

    override fun getPosition(): Vec3 =
        sourceEntity?.eyePosition() ?: Vec3.ZERO

    override fun getLookAngle(): Vec3 =
        sourceEntity?.lookAngle ?: Vec3.ZERO

    override fun isPanningDisabled(): Boolean =
        sourceEntity == getListener() || super.isPanningDisabled()

    private val sourceEntity: Entity?
        get() {
            return Minecraft.getInstance().level?.getEntity(sourceInfo.entityId)
        }
}

package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import su.plo.lib.mod.extensions.eyePosition
import su.plo.voice.client.BaseVoiceClient
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.client.extension.transformationOffset
import su.plo.voice.proto.data.audio.source.EntitySourceInfo

class ClientEntitySource(
    voiceClient: BaseVoiceClient,
    config: VoiceClientConfig,
    sourceInfo: EntitySourceInfo
) : BaseClientAudioSource<EntitySourceInfo>(voiceClient, config, sourceInfo) {

    override fun getPosition(): Vec3 {
        val entity = sourceEntity ?: return Vec3.ZERO
        val position = entity.eyePosition()
        if (entity !is Display) return position

        return position.add(entity.transformationOffset() ?: return position)
    }

    override fun getLookAngle(): Vec3 =
        sourceEntity?.lookAngle ?: Vec3.ZERO

    override fun isPanningDisabled(): Boolean =
        sourceEntity == getListener() || super.isPanningDisabled()

    override fun toString(): String =
        "ClientEntitySource{" +
                "entityId=${sourceInfo.entityId}, " +
                "entityUuid=${sourceEntity?.uuid}, " +
                "sourceLine=${sourceLine.name}, " +
                "lastSequenceNumber=${lastSequenceNumber}" +
                "}"

    private val sourceEntity: Entity?
        get() {
            return Minecraft.getInstance().level?.getEntity(sourceInfo.entityId)
        }
}

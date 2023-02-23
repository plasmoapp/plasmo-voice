package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.proto.data.audio.source.EntitySourceInfo
import java.util.*

class ClientEntitySource(
    voiceClient: PlasmoVoiceClient,
    config: ClientConfig,
    sourceInfo: EntitySourceInfo
) : BaseClientAudioSource<EntitySourceInfo>(voiceClient, config, sourceInfo) {

    override fun getPosition(position: FloatArray): FloatArray {
        sourceEntity?.let { entity ->
            position[0] = entity.x.toFloat()
            position[1] = (entity.y + entity.eyeHeight).toFloat()
            position[2] = entity.z.toFloat()
        }
        return position
    }

    override fun getLookAngle(lookAngle: FloatArray): FloatArray {
        sourceEntity?.let { entity ->
            val entityLookAngle = entity.lookAngle
            lookAngle[0] = entityLookAngle.x.toFloat()
            lookAngle[1] = entityLookAngle.y.toFloat()
            lookAngle[2] = entityLookAngle.z.toFloat()
        }
        return lookAngle
    }

    private val sourceEntity: Entity?
        get() {
            return Minecraft.getInstance().level?.getEntity(sourceInfo.entityId)
        }
}

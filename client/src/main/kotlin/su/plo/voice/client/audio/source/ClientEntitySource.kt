package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.proto.data.audio.source.EntitySourceInfo
import java.util.*

class ClientEntitySource(
    voiceClient: PlasmoVoiceClient,
    config: ClientConfig,
    sourceInfo: EntitySourceInfo
) : BaseClientAudioSource<EntitySourceInfo>(voiceClient, config, sourceInfo) {

    override fun getPosition(): Vec3 =
        sourceEntity?.eyePosition ?: Vec3.ZERO

    override fun getLookAngle(): Vec3 =
        sourceEntity?.lookAngle ?: Vec3.ZERO

    private val sourceEntity: Entity?
        get() {
            return Minecraft.getInstance().level?.getEntity(sourceInfo.entityId)
        }
}

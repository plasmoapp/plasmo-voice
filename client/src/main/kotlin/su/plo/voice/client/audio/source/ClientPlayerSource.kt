package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.player.Player
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.client.config.ClientConfig
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo
import java.util.*

class ClientPlayerSource(
    voiceClient: PlasmoVoiceClient,
    config: ClientConfig,
    sourceInfo: PlayerSourceInfo
) : BaseClientAudioSource<PlayerSourceInfo>(voiceClient, config, sourceInfo) {

    override fun getPosition(position: FloatArray): FloatArray {
        sourcePlayer?.let { player ->
            position[0] = player.x.toFloat()
            position[1] = (player.y + player.eyeHeight).toFloat()
            position[2] = player.z.toFloat()
        }
        return position
    }

    override fun getLookAngle(lookAngle: FloatArray): FloatArray {
        sourcePlayer?.let { player ->
            val playerLookAngle = player.lookAngle
            lookAngle[0] = playerLookAngle.x.toFloat()
            lookAngle[1] = playerLookAngle.y.toFloat()
            lookAngle[2] = playerLookAngle.z.toFloat()
        }
        return lookAngle
    }

    private val sourcePlayer: Player?
        get() {
            return Minecraft.getInstance().level?.getPlayerByUUID(sourceInfo.playerInfo.playerId)
        }
}

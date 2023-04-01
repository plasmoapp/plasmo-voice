package su.plo.voice.client.audio.source

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import su.plo.config.entry.BooleanConfigEntry
import su.plo.config.entry.DoubleConfigEntry
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket

class ClientPlayerSource(
    voiceClient: PlasmoVoiceClient,
    config: VoiceClientConfig,
    sourceInfo: PlayerSourceInfo
) : BaseClientAudioSource<PlayerSourceInfo>(voiceClient, config, sourceInfo) {

    override var sourceVolume: DoubleConfigEntry = config.voice
        .volumes
        .getVolume("source_${sourceInfo.playerInfo.playerId}")

    override fun process(packet: SourceAudioPacket) {
        if (sourceMute.value()) return
        super.process(packet)
    }

    override fun process(packet: SourceAudioEndPacket) {
        if (sourceMute.value()) return
        super.process(packet)
    }

    override fun getPosition(): Vec3 =
        sourcePlayer?.eyePosition ?: Vec3.ZERO

    override fun getLookAngle(): Vec3 =
        sourcePlayer?.lookAngle ?: Vec3.ZERO

    override fun isPanningDisabled(): Boolean =
        sourcePlayer == getListener() || super.isPanningDisabled()

    private val sourceMute: BooleanConfigEntry
        get() {
            return config.voice
                .volumes
                .getMute("source_" + sourceInfo.playerInfo.playerId)
        }

    private val sourcePlayer: Player?
        get() {
            return Minecraft.getInstance().level?.getPlayerByUUID(sourceInfo.playerInfo.playerId)
        }
}

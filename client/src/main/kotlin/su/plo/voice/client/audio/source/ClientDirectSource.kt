package su.plo.voice.client.audio.source

import kotlinx.coroutines.runBlocking
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.phys.Vec3
import su.plo.config.entry.BooleanConfigEntry
import su.plo.config.entry.DoubleConfigEntry
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.AlAudioDevice
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.device.source.AlSource
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.client.utils.toVec3
import su.plo.voice.proto.data.audio.source.DirectSourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket

class ClientDirectSource(
    voiceClient: PlasmoVoiceClient,
    config: VoiceClientConfig,
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
        runBlocking { updateSourceParams() }
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

    override fun getPosition(): Vec3 {
        if (sourceInfo.relativePosition != null) {
            return if (sourceInfo.isCameraRelative) {
                sourceInfo.relativePosition!!.toVec3()
            } else {
                getAbsoluteSourcePosition()
            }
        }

        return Vec3.ZERO
    }

    override fun getLookAngle(): Vec3 {
        if (sourceInfo.lookAngle == null || sourceInfo.relativePosition == null)
            return Vec3.ZERO

        return sourceInfo.lookAngle!!.toVec3()
    }

    override fun shouldCalculateOcclusion() =
        sourceInfo.relativePosition != null && super.shouldCalculateOcclusion()

    override fun isPanningDisabled(): Boolean {
        return sourceInfo.isCameraRelative || super.isPanningDisabled()
    }

    override fun calculateDistanceGain(sourceDistance: Double, maxDistance: Double) =
        if (sourceInfo.isCameraRelative) 1.0
        else super.calculateDistanceGain(sourceDistance, maxDistance)

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

    private fun getAbsoluteSourcePosition(): Vec3 {
        return sourceInfo.relativePosition?.let {
            val player: LocalPlayer = Minecraft.getInstance().player ?: return Vec3.ZERO

            return Vec3(
                player.x + it.x,
                player.y + player.eyeHeight + it.y,
                player.z + it.z
            )
        } ?: Vec3.ZERO
    }

    private suspend fun updateSourceParams() {
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

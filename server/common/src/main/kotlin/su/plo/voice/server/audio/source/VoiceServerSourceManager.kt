package su.plo.voice.server.audio.source

import com.google.common.collect.Maps
import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.*
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.codec.CodecInfo
import java.util.*

class VoiceServerSourceManager(private val voiceServer: PlasmoVoiceServer) : ServerSourceManager {

    private val sourceById: MutableMap<UUID, ServerAudioSource<*>> = Maps.newConcurrentMap()

    override fun getSourceById(sourceId: UUID): Optional<ServerAudioSource<*>> {
        return Optional.ofNullable(sourceById[sourceId])
    }

    override fun getSources(): Collection<ServerAudioSource<*>> {
        return sourceById.values
    }

    override fun clear() {
        sourceById.clear()
    }

    override fun createDirectSource(
        addonObject: Any,
        line: ServerSourceLine,
        stereo: Boolean,
        decoderInfo: CodecInfo?
    ): ServerDirectSource {
        val addon = validateAddon(addonObject)

        return VoiceServerDirectSource(
            voiceServer,
            voiceServer.udpConnectionManager,
            addon,
            line,
            decoderInfo,
            stereo
        ).also { sourceById[it.id] = it }
    }

    override fun remove(sourceId: UUID) {
        sourceById.remove(sourceId)
    }

    override fun remove(source: ServerAudioSource<*>) {
        sourceById.remove(source.id)
    }

    override fun createPlayerSource(
        addonObject: Any,
        player: VoiceServerPlayer,
        line: ServerSourceLine,
        stereo: Boolean,
        decoderInfo: CodecInfo?
    ): ServerPlayerSource {
        val addon = validateAddon(addonObject)

        return VoiceServerPlayerSource(
            voiceServer,
            addon,
            line,
            decoderInfo,
            stereo,
            player
        ).also { sourceById[it.id] = it }
    }

    override fun createEntitySource(
        addonObject: Any,
        entity: MinecraftServerEntity,
        line: ServerSourceLine,
        stereo: Boolean,
        decoderInfo: CodecInfo?
    ): ServerEntitySource {
        val addon = validateAddon(addonObject)

        if (entity is MinecraftServerPlayerEntity)
            throw java.lang.IllegalArgumentException("For creating sources for players, use createPlayerSource instead")

        return VoiceServerEntitySource(
            voiceServer,
            addon,
            line,
            decoderInfo,
            stereo,
            entity
        ).also { sourceById[it.id] = it }
    }

    override fun createStaticSource(
        addonObject: Any,
        position: ServerPos3d,
        line: ServerSourceLine,
        stereo: Boolean,
        decoderInfo: CodecInfo?
    ): ServerStaticSource {
        val addon = validateAddon(addonObject)

        return VoiceServerStaticSource(
            voiceServer,
            addon,
            line,
            decoderInfo,
            stereo,
            position
        ).also { sourceById[it.id] = it }
    }

    private fun validateAddon(addonObject: Any): AddonContainer =
        voiceServer.addonManager.getAddon(addonObject)
            .orElseThrow { IllegalArgumentException("addonObject is not an addon") }
}

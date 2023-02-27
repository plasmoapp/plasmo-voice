package su.plo.voice.server.audio.capture

import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.capture.BaseProximityServerActivation
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.capture.VoiceActivation
import su.plo.voice.proto.data.audio.line.VoiceSourceLine
import su.plo.voice.server.config.VoiceServerConfig

class ProximityServerActivation(voiceServer: PlasmoVoiceServer) :
    BaseProximityServerActivation(voiceServer, "proximity", PermissionDefault.TRUE) {

    private var activation: ServerActivation? = null

    fun register(config: VoiceServerConfig) {
        voiceServer.activationManager.unregister(VoiceActivation.PROXIMITY_ID)
        voiceServer.sourceLineManager.unregister(VoiceActivation.PROXIMITY_ID)

        val builder = voiceServer.activationManager.createBuilder(
            voiceServer,
            VoiceActivation.PROXIMITY_NAME,
            "pv.activation.proximity",
            "plasmovoice:textures/icons/microphone.png",
            "pv.activation.proximity",
            1
        )
        activation = builder
            .setDistances(config.voice().proximity().distances())
            .setDefaultDistance(config.voice().proximity().defaultDistance())
            .setProximity(true)
            .setTransitive(true)
            .setStereoSupported(false)
            .build()

        voiceServer.sourceLineManager.register(
            voiceServer,
            VoiceSourceLine.PROXIMITY_NAME,
            "pv.activation.proximity",
            "plasmovoice:textures/icons/speaker.png",
            1
        )
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onPlayerSpeak(event: PlayerSpeakEvent) {
        val player = event.player as VoiceServerPlayer
        val packet = event.packet

        if (dropPacket(player, packet.distance.toInt())) return

        getPlayerSource(player, packet.activationId, packet.isStereo)?.let { source ->
            sendAudioPacket(player, source, packet)
        }
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onPlayerSpeakEnd(event: PlayerSpeakEndEvent) {
        val player = event.player as VoiceServerPlayer
        val packet = event.packet

        if (dropPacket(player, packet.distance.toInt())) return

        getPlayerSource(player, packet.activationId, null)?.let { source ->
            sendAudioEndPacket(source, packet)
        }
    }

    private fun dropPacket(player: VoiceServerPlayer, distance: Int): Boolean =
        activation?.checkPermissions(player) == false ||
                !voiceServer.config
                    .voice()
                    .proximity()
                    .distances()
                    .contains(distance)
}

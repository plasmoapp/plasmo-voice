package su.plo.voice.server.audio.capture

import su.plo.lib.api.server.permission.PermissionDefault
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.capture.ProximityServerActivationHelper
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent
import su.plo.voice.api.server.event.player.PlayerActivationDistanceUpdateEvent
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.capture.VoiceActivation
import su.plo.voice.proto.data.audio.line.VoiceSourceLine
import su.plo.voice.server.config.VoiceServerConfig

class ProximityServerActivation(private val voiceServer: PlasmoVoiceServer) {

    private var proximityHelper: ProximityServerActivationHelper? = null

    fun register(config: VoiceServerConfig) {

        proximityHelper?.let {
            voiceServer.eventBus.unregister(voiceServer, it)
            voiceServer.activationManager.unregister(it.activation)
            voiceServer.sourceLineManager.unregister(it.sourceLine)
        }

        val builder = voiceServer.activationManager.createBuilder(
            voiceServer,
            VoiceActivation.PROXIMITY_NAME,
            "pv.activation.proximity",
            "plasmovoice:textures/icons/microphone.png",
            "pv.activation.proximity",
            1
        )
        val activation = builder
            .setDistances(config.voice().proximity().distances())
            .setDefaultDistance(config.voice().proximity().defaultDistance())
            .setProximity(true)
            .setTransitive(true)
            .setStereoSupported(false)
            .setPermissionDefault(PermissionDefault.TRUE)
            .build()

        val sourceLine = voiceServer.sourceLineManager.register(
            voiceServer,
            VoiceSourceLine.PROXIMITY_NAME,
            "pv.activation.proximity",
            "plasmovoice:textures/icons/speaker.png",
            1
        )

        proximityHelper = ProximityServerActivationHelper(voiceServer, activation, sourceLine)
        voiceServer.eventBus.register(voiceServer, proximityHelper!!)
    }

    @EventSubscribe
    fun onActivationDistanceChange(event: PlayerActivationDistanceUpdateEvent) {
        if (event.activation.id != VoiceActivation.PROXIMITY_ID) return
        if (event.oldDistance == -1) return
        event.player.visualizeDistance(event.distance)
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onPlayerSpeak(event: PlayerSpeakEvent) {
        val player = event.player as VoiceServerPlayer
        val packet = event.packet

        if (dropPacket(player, packet.distance.toInt())) return

        proximityHelper!!.getPlayerSource(player, packet.activationId, packet.isStereo)?.let { source ->
            proximityHelper!!.sendAudioPacket(player, source, packet)
        }
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onPlayerSpeakEnd(event: PlayerSpeakEndEvent) {
        val player = event.player as VoiceServerPlayer
        val packet = event.packet

        if (dropPacket(player, packet.distance.toInt())) return

        proximityHelper!!.getPlayerSource(player, packet.activationId, null)?.let { source ->
            proximityHelper!!.sendAudioEndPacket(source, packet)
        }
    }

    private fun dropPacket(player: VoiceServerPlayer, distance: Int): Boolean =
        proximityHelper?.activation?.checkPermissions(player) == false || !voiceServer.config
            .voice()
            .proximity()
            .distances()
            .contains(distance)
}

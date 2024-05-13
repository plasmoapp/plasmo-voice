package su.plo.voice.api.server.audio.capture

import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket

/**
 * Information contains the player and the activation id activated by this player.
 */
data class PlayerActivationInfo(
    val player: VoicePlayer,
    val audioPacket: PlayerAudioPacket
)

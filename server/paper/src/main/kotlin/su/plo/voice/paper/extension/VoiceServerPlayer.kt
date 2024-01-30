package su.plo.voice.paper.extension

import org.bukkit.entity.Player
import su.plo.voice.api.server.player.VoiceServerPlayer

/**
 * Gets a Bukkit [Player] of [VoiceServerPlayer].
 *
 * @return The Bukkit player.
 */
fun VoiceServerPlayer.asBukkitPlayer(): Player =
    instance.getInstance()

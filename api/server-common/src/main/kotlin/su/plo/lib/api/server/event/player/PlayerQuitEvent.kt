package su.plo.lib.api.server.event.player

import su.plo.lib.api.event.MinecraftGlobalEvent
import su.plo.lib.api.server.player.MinecraftServerPlayer

/**
 * This event is fires once the player is disconnected from the server
 */
object PlayerQuitEvent
    : MinecraftGlobalEvent<PlayerQuitEvent.Callback>(
    { callbacks ->
        Callback { player ->
            callbacks.forEach { callback -> callback.onPlayerQuit(player) }
        }
    }
) {
    fun interface Callback {

        fun onPlayerQuit(player: MinecraftServerPlayer)
    }
}

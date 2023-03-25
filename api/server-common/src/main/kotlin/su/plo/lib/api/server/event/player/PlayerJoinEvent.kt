package su.plo.lib.api.server.event.player

import su.plo.lib.api.event.MinecraftGlobalEvent
import su.plo.lib.api.server.player.MinecraftServerPlayer

/**
 * This event is fired once the player is joined the server
 */
object PlayerJoinEvent
    : MinecraftGlobalEvent<PlayerJoinEvent.Callback>(
    { callbacks ->
        Callback { player ->
            callbacks.forEach { callback -> callback.onPlayerJoin(player) }
        }
    }
) {
    fun interface Callback {

        fun onPlayerJoin(player: MinecraftServerPlayer)
    }
}

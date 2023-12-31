package su.plo.voice.proxy.event.player

import su.plo.lib.api.event.MinecraftGlobalEvent
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo
import su.plo.voice.proxy.event.player.McProxyServerConnectedEvent.Callback


/**
 * An event fired after the player was successfully connected to the server.
 */
object McProxyServerConnectedEvent
    : MinecraftGlobalEvent<Callback>(
    { callbacks ->
        Callback { player, previousServer ->
            callbacks.forEach { callback -> callback.onServerConnected(player, previousServer) }
        }
    }
) {
    fun interface Callback {

        fun onServerConnected(player: MinecraftProxyPlayer, previousServer: MinecraftProxyServerInfo?)
    }
}

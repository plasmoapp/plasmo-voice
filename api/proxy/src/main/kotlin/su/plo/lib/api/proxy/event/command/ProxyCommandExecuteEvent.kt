package su.plo.lib.api.proxy.event.command

import su.plo.lib.api.event.MinecraftGlobalEvent
import su.plo.lib.api.server.command.MinecraftCommandSource

/**
 * This event is fired right before command execution
 */
object ProxyCommandExecuteEvent
    : MinecraftGlobalEvent<ProxyCommandExecuteEvent.Callback>(
    { callbacks ->
        Callback { source, command ->
            callbacks.forEach { callback -> callback.onCommandExecute(source, command) }
        }
    }
) {
    fun interface Callback {

        fun onCommandExecute(source: MinecraftCommandSource, command: String)
    }
}

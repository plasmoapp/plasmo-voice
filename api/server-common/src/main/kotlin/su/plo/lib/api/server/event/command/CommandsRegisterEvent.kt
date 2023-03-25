package su.plo.lib.api.server.event.command

import su.plo.lib.api.event.MinecraftGlobalEvent
import su.plo.lib.api.server.MinecraftCommonServerLib
import su.plo.lib.api.server.command.MinecraftCommand
import su.plo.lib.api.server.command.MinecraftCommandManager
import su.plo.lib.api.server.event.command.CommandsRegisterEvent.Callback

abstract class CommandsRegisterEvent<C : MinecraftCommand, S : MinecraftCommonServerLib>
    : MinecraftGlobalEvent<Callback<C, S>>(
    { callbacks ->
        Callback { commandManager, minecraftServer ->
            callbacks.forEach { callback -> callback.onCommandsRegister(commandManager, minecraftServer) }
        }
    }
) {
    fun interface Callback<C : MinecraftCommand, S : MinecraftCommonServerLib> {

        fun onCommandsRegister(commandManager: MinecraftCommandManager<C>, minecraftServer: S)
    }
}

package su.plo.lib.paper.command

import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.MinecraftServerLib
import su.plo.lib.api.server.command.MinecraftCommand
import su.plo.lib.api.server.command.MinecraftCommandManager
import su.plo.lib.paper.chat.BaseComponentTextConverter

class PaperCommandManager(
    private val minecraftServer: MinecraftServerLib,
    private val textConverter: BaseComponentTextConverter
) : MinecraftCommandManager<MinecraftCommand?>() {

    @Synchronized
    fun registerCommands(loader: JavaPlugin) {
        commandByName.forEach { (name, command) ->
            val paperCommand = PaperCommand(minecraftServer, textConverter, command!!, name!!)
            loader.server.commandMap.register("plasmovoice", paperCommand)
        }

        registered = true
    }
}
